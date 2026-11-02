package shopflow.w07.importer;

public record ImportResult(boolean success, int imported, int skipped) {}
