package dev.aenco.mydash.core;

public final class FileEntrySnapshot {
    private final String path;
    private final String name;
    private final boolean directory;
    private final boolean symlink;
    private final boolean editable;
    private final long size;

    FileEntrySnapshot(
        String path,
        String name,
        boolean directory,
        boolean symlink,
        boolean editable,
        long size
    ) {
        this.path = path;
        this.name = name;
        this.directory = directory;
        this.symlink = symlink;
        this.editable = editable;
        this.size = size;
    }

    public String path() { return path; }
    public String name() { return name; }
    public boolean directory() { return directory; }
    public boolean symlink() { return symlink; }
    public boolean editable() { return editable; }
    public long size() { return size; }
}
