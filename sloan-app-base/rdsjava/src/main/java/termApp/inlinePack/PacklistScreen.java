package termApp.inlinePack;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

import static termApp.util.Constants.*;

public class PacklistScreen extends AbstractInLinePackStationScreen {

    private Button ok, cancel, reprint;
    private int printJobSeq;
    private boolean checkPrintJob;
    private Button markRepack;
    private Button markException;

    public PacklistScreen(TerminalDriver term) {
        super(term);
        setLogoutAllowed(false);
        checkPrintJob = false;
        reset = 1000L;
    }

    // init

    @Override
    public void initDisplay() {
        initInfoBox();
        setInfoBox(false);
        initButtons();
        initPromptModule(
                "Printing packlist",
                ENTRYCODE_NONE,
                true,
                true);
        startPrintJob();
        super.initDisplay();
    }

    protected void initButtons() {
        int x = MARGIN;
        int y = BTN_Y;
        int f = BTN_FONT;
        int width = 600;
        ok = new Button(x, y, f, "Ok", Align.LEFT, -1, -1, COLOR_WHITE, false);
        ok.registerOnActionListener(okAction());
        ok.hide();

        x = W1_2;
        reprint = new Button(x, y, f, "Reprint", Align.CENTER, -1, -1, COLOR_WHITE, false);
        reprint.registerOnActionListener(reprintAction());
        reprint.hide();

        x = SCREEN_WIDTH - MARGIN;
        cancel = new Button(x, y, f, "Cancel", Align.RIGHT, -1, -1, COLOR_YELLOW, true);
        cancel.registerOnActionListener(cancelAction());
        cancel.show();

        y = 200;
        x = SCREEN_WIDTH - 750;
        markRepack = new Button(x, y, f, "Mark Repack", Align.LEFT, width, false);
        markRepack.registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
                markRepack();
            }
        });
        markRepack.show();

        y += 150;
        markException = new Button(x, y, f, "Mark Exception", Align.LEFT, width, false);
        markException.registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
                markException();
            }
        });
        markException.show();
    }

    @Override
    protected void doOkay() {
        inform("OK button pressed/timer expired");
        clearRuntime();
        setNextScreen("inlinePack.OrderLabelScreen");
    }

    @Override
    protected void doReprint() {
        inform("Reprint button pressed");
        showActionMsg("Reprinting packlist");
        startPrintJob();
    }

    @Override
    protected void doCancel() {
        inform("Cancel button pressed");
        clearRuntime();
        setNextScreen("inlinePack.IdleScreen");
    }

    // logic

    private void startPrintJob() {
        inform("starting print job for packlist");
        initProcessTimer();
        printJobSeq = printPacklist();
        inform("print job sequence [%d] for packlist.", printJobSeq);
        if (printJobSeq > 0) {
            checkPrintJob = true;
        } else {
            if (printJobSeq == PRINT_ERROR_DATABASE) {
                showAlertMsg("Error connecting to database");
            } else if (printJobSeq == PRINT_ERROR_NOLABEL) {
                showAlertMsg("No document found");
            } else {
                showAlertMsg("Something went wrong");
            }
        }
    }

    public void handleTick() {
        super.handleTick();
        checkPrintJob();
    }

    private void checkPrintJob() {
        if (checkPrintJob) {
            int complete = getPrintJobStatus(printJobSeq);
            inform("status of print job [%d] is [%d]", printJobSeq, complete);
            if (complete == 1) {
                showSuccessMsg("Printing complete");
                reprint.show();
                ok.show();
                checkPrintJob = false;
                initStartTimer();
            } else {
                if (processing == 0)
                    initProcessTimer();
            }
        }
        if (checkPrintJob && processing > 0 && System.currentTimeMillis() - processing > timeout) {
            showAlertMsg("Timeout printing. Check document printer");
            checkPrintJob = false;
            processing = 0;
            reprint.show();
        }
    }

}