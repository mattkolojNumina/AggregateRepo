package dataprep;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CreatePaktTestData {

    private static final String STATUS_PICKED = "Picked";
    private static final String STATUS_SHORT = "Short";
    private static final String STATUS_PACKED = "Packed";
    private static final String STATUS_CANCELED = "Canceled";
    private static final String STATUS_UNKNOWN_LPN = "Unknown LPN";
    private static final String STATUS_NO_READ_AT_PACK = "No Read @ Pack";
    private static final String STATUS_NO_READ_AT_PNA = "No Read @ PNA";

    public static void main(String[] args) throws Exception {

        // read data from csv file
        File f = new File("C:\\Users\\JohnWickstrom\\Documents\\NuminaGroup\\Projects\\Sloane\\PackTestingScenarios.csv");
        Path p = f.toPath();
        List<String> rows = Files.readAllLines(p);

        List<RDSCarton> cartons = new ArrayList<>();

        for( int i = 1; i < rows.size(); i++ ) {
            String row = rows.get(i);

            // create carton record from file data
            RDSCarton carton = createCarton(row.split(","));
            if (carton != null) {
                cartons.add(carton);
            }
        }

        for(RDSCarton carton: cartons) {
            System.out.println(carton.lpn);
            carton.save();
        }
    }

    private static RDSCarton createCarton(String[] fields) {

        RDSCarton carton = new RDSCarton();

        carton.lpn = fields[0];
        carton.type = fields[2];

        String status = fields[4];
        if (status == null || STATUS_UNKNOWN_LPN.equals(status)) {
            return null;
        }

        Instant now = Instant.now();

        switch (status) {
            case STATUS_PICKED, STATUS_NO_READ_AT_PACK, STATUS_NO_READ_AT_PNA -> carton.pickStamp = now;
            case STATUS_SHORT -> {
                carton.pickStamp = now;
                carton.shortStamp = now;
            }
            case STATUS_PACKED -> {
                carton.pickStamp = now;
                carton.packStamp = now;
            }
            case STATUS_CANCELED -> {
                carton.pickStamp = now;
                carton.cancelStamp = now;
            }
        }

        carton.auditRequired = translateFlag(fields[5]);
        carton.repackRequired = translateFlag(fields[6]);
        carton.packException = translateFlag(fields[7]);

        return carton;
    }

    public static boolean translateFlag(String flag) {
        return "Y".equals(flag);
    }
}
