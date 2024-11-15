
package termApp.inlinePack;

import static rds.RDSLog.*;

import java.util.regex.Pattern;

import dao.SloaneCommonDAO;
import dao.AbstractDAO;

import java.util.Map;

import term.TerminalDriver;
import termApp.*;
import rds.RDSUtil;

/**
 * Base class for terminal screens, defining constants and default
 * behavior.
 */
public abstract class AbstractInLinePackStationScreen extends AbstractProjectAppScreen {

    protected static final String PRINTER_PACKLIST = "packlist_printer";

    // generic code
    protected static final int CODE_ERROR = -1;

    // carton mode

    // carton status
    protected static final String CARTON_STATUS_PICKING = "PICKING";
    protected static final String CARTON_STATUS_PICKED = "PICKED";
    protected static final String CARTON_STATUS_SHORT = "SHORT";
    protected static final String CARTON_STATUS_LABELED = "LABELED";
    protected static final String CARTON_STATUS_PACKED = "PACKED";
    protected static final String CARTON_STATUS_AUDITED = "AUDITED";
    protected static final String CARTON_STATUS_VASDONE = "VASDONE";
    protected static final String CARTON_STATUS_QC = "QC";
    protected static final String CARTON_STATUS_CANCELED = "CANCELED";
    protected static final String CARTON_STATUS_AUDIT_REQUIRED = "AUDIT_REQUIRED";
    protected static final String CARTON_STATUS_REPACK_REQUIRED = "REPACK_REQUIRED";
    protected static final String CARTON_STATUS_PACK_EXCEPTION = "PACK_EXCEPTION";

    protected static final String CARTON_LPN_REGEX = "C[A-Z0-9]{6}";
    protected static final String TOTE_LPN_REGEX = "T[A-Z0-9]{6}";
    protected static final String PALLET_LPN_REGEX = "P[A-Z0-9]{8}";

    protected static final int INVALID_CARTON = -1;
    protected static final int PRINT_PACKLIST = 3;
    protected static final int PRINT_SHIPLABEL = 4;
    protected static final int WEIGHT_CARTON = 5;

    protected static final int PRINT_ERROR_DATABASE = -2;
    protected static final int PRINT_ERROR_NOLABEL = -1;

    private String startScreen;
    protected String runtimeLPN;
    protected String preZone;

    public static enum TermParam {
        scan, cartonLpn, cartonSeq, cartonUcc, pickType, cartonType, cartonStatus, trackingNumber,
        shipmentType, shipmentId, orderId,
        estWeight, estLength, estHeight, estWidth, actLength, actWidth, actHeight, actWeight, dimension,
        labelStatus, sku, shipId,
        labelSeq, needLength, needWidth, needHeight, ucc, nextLabelSeq,
        palletLpn, palletSeq, palletUcc, expCount,
        box, packlistSeq,
    }

    ;

    /**
     * Constructs a screen with default values.
     */
    public AbstractInLinePackStationScreen(TerminalDriver term) {
        super(term);
        startScreen = "inlinePack.IdleScreen";
        setAllDatabase();
        preZone = getParam("preZone");
        runtimeLPN = getParam("runtimeLPN");
        numOfInfoRows = 1;
        AbstractDAO.setDatabase(db);
    }

    // display methods
    @Override
    public void setLeftInfoBox() {
        if (leftInfoBox == null)
            return;
        String cartonID = getStrParam(TermParam.cartonLpn);
        leftInfoBox.updateInfoPair(0, "Carton", cartonID);
        leftInfoBox.show();
    }

    @Override
    public void setRightInfoBox() {
        if (rightInfoBox == null)
            return;
        rightInfoBox.hide();
    }

    // logic

    // ProcessScan, ProcessBox methods

    protected boolean isTote(String barcode) {
        return barcode.matches(TOTE_LPN_REGEX);
    }

    protected int lookupCartonByLpn(String barcode) {
        if (barcode.matches(TOTE_LPN_REGEX)) {
            return db.getInt(-1, 
                "SELECT cartonSeq FROM rdsCartons " +
                "WHERE lpn='%s' AND shipStamp IS NULL " +
                "ORDER BY cartonSeq DESC LIMIT 1", barcode
            );
        }
        return db.getInt(-1, 
            "SELECT cartonSeq FROM rdsCartons " +
            "WHERE lpn='%s' " +
            "ORDER BY cartonSeq DESC LIMIT 1", barcode
        );
    }

    protected void setCarton(int cartonSeq) {
        Map<String, String> map = db.getRecordMap(
            "SELECT * FROM rdsCartons " +
            "WHERE cartonSeq=%d", cartonSeq
        );
        String cartonLpn = getMapStr(map, "lpn");
        String orderId = getMapStr(map, "orderId");
        String pickType = getMapStr(map, "pickType");
        String cartonType = getMapStr(map, "cartonType");
        boolean canceled = !getMapStr(map, "cancelStamp").isEmpty();
        boolean picked = !getMapStr(map, "pickStamp").isEmpty();
        boolean shortPicked = !getMapStr(map, "pickShortStamp").isEmpty();
        boolean audited = !getMapStr(map,"auditStamp").isEmpty();
        boolean packed = !getMapStr(map, "packStamp").isEmpty();
        boolean auditRequired = getMapStr(map, "auditRequired").equals("1");
        if( !auditRequired && !audited)
      	  auditRequired = SloaneCommonDAO.cartonRequireAudit(cartonSeq);
        boolean repackRequired = getMapStr(map, "repackRequired").equals("1");
        //boolean packException = getMapStr(map, "packException").equals("1");

        String cartonStatus = (canceled) ? CARTON_STATUS_CANCELED :
                //  (audited)? CARTON_STATUS_AUDITED:
                (auditRequired) ? CARTON_STATUS_AUDIT_REQUIRED :
                        (repackRequired) ? CARTON_STATUS_REPACK_REQUIRED :
                        	     (packed) ? CARTON_STATUS_PACKED :
                                        (picked) ? CARTON_STATUS_PICKED :
                                                   (shortPicked) ? CARTON_STATUS_SHORT : CARTON_STATUS_PICKING;
        setParam(TermParam.cartonSeq, "" + cartonSeq);
        setParam(TermParam.cartonLpn, cartonLpn);
        setParam(TermParam.pickType, pickType);
        setParam(TermParam.cartonType, cartonType);
        setParam(TermParam.cartonStatus, cartonStatus);
        setParam(TermParam.orderId, orderId);
    }

    protected void markCartonPacked() {
        int cartonSeq = getIntParam(TermParam.cartonSeq);
        // AppCommon.triggerCartonPacked(cartonSeq, getOperatorId());
        db.execute(
                "UPDATE rdsCartons " +
                        "SET packStamp=NOW() " +
                        "WHERE cartonSeq=%d", cartonSeq
        );
        trace("cartonSeq [%d] packed at inline pack station", cartonSeq);
    }

    /*
     * Determines if both the order label and packlist for carton exist.
     */
    protected int getDocuments() {
        boolean orderLabel = getOrderLabel();
        boolean packlist = getPacklist();

        if (!orderLabel) {
            inform("Carton [%s] failed to get orderLabel", getParam(TermParam.cartonLpn));
            return -1;
        }
        if (!packlist) {
            inform("Carton [%s] failed to get packlist", getParam(TermParam.cartonLpn));
            return -2;
        }
        return 1;
    }

    /*
     * Determines if an order label exists for a carton
     */
    protected boolean getOrderLabel() {
        int cartonSeq = getIntParam(TermParam.cartonSeq);
        int orderLabelSeq = db.getInt(-1,
            "SELECT docSeq FROM rdsDocuments " +
            "WHERE docType = 'label' AND refValue = %d " +
            "ORDER BY docSeq DESC LIMIT 1", cartonSeq
        );
        if (orderLabelSeq < 0) return false;
        setParam(TermParam.labelSeq, "" + orderLabelSeq);
        return true;
    }

    /*
     * Determines if a packlist exists for a carton
     */
    protected boolean getPacklist() {
        int cartonSeq = getIntParam(TermParam.cartonSeq);
        int packlistSeq = db.getInt(-1,
            "SELECT docSeq FROM rdsDocuments " +
            "WHERE docType = 'packlist' AND refValue = %d " +
            "ORDER BY docSeq DESC LIMIT 1", cartonSeq
        );
        if (packlistSeq < 0) return false;
        setParam(TermParam.packlistSeq, "" + packlistSeq);
        trace("Found packlist with docSeq [%d] for cartonSeq [%d]", packlistSeq, cartonSeq);
        return true;
    }

    /*
     * Prints the packlist
     */
    protected int printOrderLabel() {
        return printLabel(getParam("labeler"), getIntParam(TermParam.labelSeq));
    }

    /*
     * Prints the packlist
     */
    protected int printPacklist() {
        return printLabel(getParam("printer"), getIntParam(TermParam.packlistSeq));
    }

    /*
     * Triggers a printer
     */
    protected int printLabel(String printerId, int docSeq) {

        int result = db.execute(
                "INSERT INTO docQueue " +
                        "SET device = '%s', " +
                        " box = 0, " +
                        " docSeq = %d",
                printerId,
                docSeq
        );
        if (result <= 0) return PRINT_ERROR_DATABASE;

        int queueSeq = db.getSequence();

        trace("Print job queued for queueSeq [%d]", queueSeq);
        return queueSeq;
    }

    /*
     * Checks a print jobs status
     */
    protected int getPrintJobStatus(int seq) {
        return db.getInt(0, "SELECT complete FROM docQueue WHERE queueSeq = %d", seq);
    }

    /*
     * Marks a carton as requiring a repack.
     */
    protected void markRepack() {
        int cartonSeq = getIntParam(TermParam.cartonSeq);
        db.execute(
                "UPDATE rdsCartons SET repackRequired=1 " +
                        "WHERE cartonSeq=%d", cartonSeq
        );
        clearRuntime();
        setNextScreen("inlinePack.IdleScreen");
    }

    /*
     * Marks a carton as having a generic exception.
     */
    protected void markException() {
        int cartonSeq = getIntParam(TermParam.cartonSeq);
        db.execute(
                "UPDATE rdsCartons SET packException=1 " +
                        "WHERE cartonSeq=%d", cartonSeq
        );
        clearRuntime();
        setNextScreen("inlinePack.IdleScreen");
    }

    // helpers

    protected void clearRuntime() {
        trace("Clearing runtime value [%s]", runtimeLPN);
        db.execute(
                "UPDATE runtime SET value='' " +
                        "WHERE name='%s'", runtimeLPN
        );
    }

    public void handleInit() {
        term.clearScreen(DEFAULT_SCREEN_COLOR);
        super.handleInit();
        header.init();
        header.updateTitle("INLINE PACK STATION");
        footer.show();
    }

    public void handleTick() {
        super.handleTick();
        cycleCheck();
        if (start > 0 && System.currentTimeMillis() - start > reset)
            doOkay();
    }

    protected void screenSetup() {
        cycle = 0;
        cycleCount = false;
        cycleMax = getIntControl("cycleMax") * 2;
    }

    protected void cycleCheck() {
        if (cycleMax > 0 && cycleCount) {
            if (cycle++ >= cycleMax) {
                hideResultMsgModule();
            }
        } else
            cycle = 0;
    }

    protected boolean isNumeric(String strNum) {
        if (strNum == null) return false;
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        return pattern.matcher(strNum).matches();
    }

    protected int getIntParam(TermParam param) {
        return RDSUtil.stringToInt(term.fetchAtom(param.toString(), ""), -1);
    }

    protected boolean getBoolParam(TermParam param) {
        return term.fetchAtom(param.toString(), "").equalsIgnoreCase("true");
    }

    protected double getDoubleParam(TermParam param) {
        return RDSUtil.stringToDouble(term.fetchAtom(param.toString(), ""), 0.00);
    }

    protected String getStrParam(TermParam param) {
        return term.fetchAtom(param.toString(), "");
    }

    protected String getParam(TermParam param) {
        return term.fetchAtom(param.toString(), null);
    }

    protected void setParam(TermParam param, String format, Object... args) {
        if (format == null)
            term.dropAtom(param.toString());
        else
            term.saveAtom(param.toString(), String.format(format, args));
    }

    protected void clearAllParam() {
        for (TermParam param : TermParam.values())
            term.dropAtom(param.toString());
    }

    protected void gotoStartScreen() {
        setNextScreen(startScreen);
    }

    @Override
    protected void doCancel() {
        inform("cancel button pressed");
        setNextScreen("inlinePack.IdleScreen");
    }

}

