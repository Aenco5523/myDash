package dev.aenco.mydash.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SafeFileStore {
    static final int MAX_EDIT_BYTES = 1024 * 1024;

    private static final Set<String> ROOT_DIRECTORIES = new HashSet<String>(
        Arrays.asList("config", "mods", "world")
    );

    private static final Set<String> ROOT_FILES = new HashSet<String>(
        Arrays.asList(
            "server.properties",
            "ops.json",
            "whitelist.json",
            "banned-players.json",
            "banned-ips.json"
        )
    );

    private static final Set<String> EDITABLE_EXTENSIONS = new HashSet<String>(
        Arrays.asList(
            "txt", "json", "json5", "toml", "properties",
            "yml", "yaml", "cfg", "conf", "ini", "mcmeta"
        )
    );

    private final Path serverDirectory;

    SafeFileStore(Path serverDirectory) {
        this.serverDirectory = serverDirectory.toAbsolutePath().normalize();
    }

    List<FileEntrySnapshot> list(String relativePath) throws IOException {
        String clean = cleanRelative(relativePath);

        if (clean.isEmpty()) {
            List<FileEntrySnapshot> root = new ArrayList<FileEntrySnapshot>();

            for (String directory : Arrays.asList("config", "mods", "world/serverconfig")) {
                Path target = resolveAllowed(directory);
                boolean exists = Files.exists(target);
                boolean symlink = exists && Files.isSymbolicLink(target);

                root.add(new FileEntrySnapshot(
                    directory,
                    leafName(directory),
                    true,
                    symlink,
                    false,
                    0L
                ));
            }

            for (String file : ROOT_FILES) {
                Path target = resolveAllowed(file);
                if (!Files.exists(target)) continue;

                root.add(new FileEntrySnapshot(
                    file,
                    file,
                    false,
                    Files.isSymbolicLink(target),
                    isEditable(file, target),
                    safeSize(target)
                ));
            }

            sort(root);
            return root;
        }

        Path directory = resolveAllowed(clean);
        rejectSymlinkTraversal(directory);

        if (!Files.exists(directory)) return Collections.emptyList();
        if (!Files.isDirectory(directory)) throw new IOException("Path is not a directory");

        List<FileEntrySnapshot> entries = new ArrayList<FileEntrySnapshot>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path child : stream) {
                String childRelative = toRelative(child);
                if (!isAllowed(childRelative)) continue;

                boolean symlink = Files.isSymbolicLink(child);
                boolean directoryEntry = !symlink && Files.isDirectory(child);

                entries.add(new FileEntrySnapshot(
                    childRelative,
                    child.getFileName().toString(),
                    directoryEntry,
                    symlink,
                    !directoryEntry && !symlink && isEditable(childRelative, child),
                    directoryEntry ? 0L : safeSize(child)
                ));
            }
        }

        sort(entries);
        return entries;
    }

    String readText(String relativePath) throws IOException {
        String clean = cleanRelative(relativePath);
        Path file = resolveAllowed(clean);
        rejectSymlinkTraversal(file);

        if (!Files.isRegularFile(file) || Files.isSymbolicLink(file)) {
            throw new IOException("Path is not a regular file");
        }

        if (!isEditable(clean, file)) {
            throw new IOException("File type is not editable");
        }

        long size = Files.size(file);
        if (size > MAX_EDIT_BYTES) throw new IOException("File is too large to edit");

        byte[] bytes = Files.readAllBytes(file);
        if (containsNul(bytes)) throw new IOException("Binary files cannot be edited");

        try {
            CharBuffer decoded = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
            return decoded.toString();
        } catch (CharacterCodingException exception) {
            throw new IOException("File is not valid UTF-8 text", exception);
        }
    }

    void writeText(String relativePath, byte[] bytes) throws IOException {
        if (bytes == null) throw new IOException("Missing file content");
        if (bytes.length > MAX_EDIT_BYTES) throw new IOException("File is too large to edit");
        if (containsNul(bytes)) throw new IOException("Binary content is not allowed");

        try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException exception) {
            throw new IOException("Content is not valid UTF-8 text", exception);
        }

        String clean = cleanRelative(relativePath);
        Path file = resolveAllowed(clean);
        rejectSymlinkTraversal(file);

        if (!isEditablePath(clean)) {
            throw new IOException("File type is not editable");
        }

        Path parent = file.getParent();
        if (parent == null || !parent.startsWith(serverDirectory)) {
            throw new IOException("Invalid file parent");
        }

        rejectSymlinkTraversal(parent);
        Files.createDirectories(parent);

        if (Files.exists(file)) {
            if (!Files.isRegularFile(file) || Files.isSymbolicLink(file)) {
                throw new IOException("Path is not a regular file");
            }
            backupExisting(clean, file);
        }

        Path temporary = file.resolveSibling(file.getFileName().toString() + ".mydash.tmp");
        Files.write(temporary, bytes);

        try {
            Files.move(
                temporary,
                file,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void backupExisting(String cleanRelative, Path file) throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
        Path backupRoot = serverDirectory.resolve(".mydash-backups").resolve(stamp);
        Path backup = backupRoot.resolve(Paths.get(cleanRelative)).normalize();

        if (!backup.startsWith(backupRoot)) {
            throw new IOException("Invalid backup path");
        }

        Path parent = backup.getParent();
        if (parent != null) Files.createDirectories(parent);

        Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path resolveAllowed(String relativePath) throws IOException {
        String clean = cleanRelative(relativePath);
        if (!isAllowed(clean)) throw new IOException("Path is outside the allowed myDash roots");

        Path target = serverDirectory.resolve(Paths.get(clean)).normalize();
        if (!target.startsWith(serverDirectory)) throw new IOException("Path traversal rejected");
        return target;
    }

    private void rejectSymlinkTraversal(Path target) throws IOException {
        Path normalized = target.toAbsolutePath().normalize();
        if (!normalized.startsWith(serverDirectory)) {
            throw new IOException("Path traversal rejected");
        }

        Path current = serverDirectory;
        Path relative = serverDirectory.relativize(normalized);

        for (Path part : relative) {
            current = current.resolve(part);
            if (Files.exists(current) && Files.isSymbolicLink(current)) {
                throw new IOException("Symlink traversal is not allowed");
            }
        }
    }

    private boolean isAllowed(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return true;

        String normalized = relativePath.replace('\\', '/');
        String first = normalized;
        int slash = normalized.indexOf('/');
        if (slash >= 0) first = normalized.substring(0, slash);

        String firstLower = first.toLowerCase(Locale.ROOT);
        if ("config".equals(firstLower) || "mods".equals(firstLower)) return true;

        if ("world".equals(firstLower)) {
            String lower = normalized.toLowerCase(Locale.ROOT);
            return "world/serverconfig".equals(lower)
                || lower.startsWith("world/serverconfig/");
        }

        return ROOT_FILES.contains(normalized.toLowerCase(Locale.ROOT));
    }

    private static String cleanRelative(String value) throws IOException {
        if (value == null || value.trim().isEmpty()) return "";

        String clean = value.trim().replace('\\', '/');
        if (clean.startsWith("/") || clean.indexOf('\0') >= 0 || clean.indexOf(':') >= 0) {
            throw new IOException("Invalid path");
        }

        Path normalized = Paths.get(clean).normalize();
        String result = normalized.toString().replace('\\', '/');

        if (".".equals(result)) return "";
        if (result.equals("..") || result.startsWith("../")) {
            throw new IOException("Path traversal rejected");
        }

        return result;
    }

    private static boolean isEditable(String relativePath, Path file) {
        try {
            return Files.isRegularFile(file)
                && !Files.isSymbolicLink(file)
                && Files.size(file) <= MAX_EDIT_BYTES
                && isEditablePath(relativePath);
        } catch (IOException exception) {
            return false;
        }
    }

    private static boolean isEditablePath(String relativePath) {
        String name = leafName(relativePath).toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return false;
        return EDITABLE_EXTENSIONS.contains(name.substring(dot + 1));
    }

    private String toRelative(Path path) {
        return serverDirectory.relativize(path.toAbsolutePath().normalize())
            .toString()
            .replace('\\', '/');
    }

    private static String leafName(String path) {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private static long safeSize(Path path) {
        try {
            return Files.isRegularFile(path) ? Files.size(path) : 0L;
        } catch (IOException exception) {
            return 0L;
        }
    }

    private static boolean containsNul(byte[] bytes) {
        for (byte value : bytes) {
            if (value == 0) return true;
        }
        return false;
    }

    private static void sort(List<FileEntrySnapshot> entries) {
        Collections.sort(entries, new Comparator<FileEntrySnapshot>() {
            @Override
            public int compare(FileEntrySnapshot left, FileEntrySnapshot right) {
                if (left.directory() != right.directory()) {
                    return left.directory() ? -1 : 1;
                }
                return left.name().compareToIgnoreCase(right.name());
            }
        });
    }
}
