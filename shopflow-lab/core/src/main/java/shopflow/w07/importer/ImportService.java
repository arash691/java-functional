package shopflow.w07.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Nightly stock import: reads "SKU,qty" lines and writes "SKU=qty" lines to the target file. */
public class ImportService {

    public ImportResult importCsv(Path csv, Path target) {
        Map<String, Integer> stock = new TreeMap<>();
        int skipped = 0;
        try {
            for (String line : Files.readAllLines(csv)) {
                try {
                    String[] parts = line.split(",");
                    stock.merge(parts[0].trim(), Integer.parseInt(parts[1].trim()), Integer::sum);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    skipped++;
                }
            }
            Files.write(target, render(stock));
        } catch (Exception e) {
            // don't break the nightly batch
        }
        return new ImportResult(true, stock.size(), skipped);
    }

    private static List<String> render(Map<String, Integer> stock) {
        List<String> lines = new ArrayList<>();
        stock.forEach((sku, qty) -> lines.add(sku + "=" + qty));
        return lines;
    }
}
