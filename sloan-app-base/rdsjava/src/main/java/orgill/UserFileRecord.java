package sloane;

import dao.DataAccessException;
import dao.SloaneCommonDAO;
import dao.ProOperatorDAO;
import host.FileRecord;
import host.StringUtils;
import records.ProOperator;

import java.util.ArrayList;
import java.util.List;

public class UserFileRecord implements FileRecord {

    private final String username;
    private final String userRealName;
    private final String password;
    private final String status;
    private static final String DISABLE = "0";
    private static final String ENABLE = "1";

    public UserFileRecord(String[] fields) {
        username = fields[0].trim();
        userRealName = fields[1].trim();
        password = fields[2].trim();
        status = fields[3].trim();
    }

    public String getUsername() {
        return username;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (getUsername() == null || getUsername().isBlank()) {
            validationErrors.add("Field [Username] is empty.");
        } else if (getUsername().length() != 5) {
            validationErrors.add("Field [Username] is not 5 characters.");
        } else if (!StringUtils.isNumeric(getUsername())) {
            validationErrors.add("Field [Username] is not numeric.");
        }

        if (getUserRealName() == null || getUserRealName().isBlank()) {
            validationErrors.add("Field [User real name] is empty.");
        } else if (getUserRealName().length() > 40) {
            validationErrors.add("Field [User real name] length exceeds 40 characters.");
        }

        if (getPassword() == null || getPassword().isBlank()) {
            validationErrors.add("Field [Password] is empty.");
        } else if (getPassword().length() != 3) {
            validationErrors.add("Field [Password] is not 3 characters.");
        }

        if (getStatus() == null || getStatus().isBlank()) {
            validationErrors.add("Field [Status] is empty.");
        } else if (!getStatus().equals(ENABLE) && !getStatus().equals(DISABLE)) {
            validationErrors.add(String.format("Field [Status] contains an unknown value [%s]", getStatus()));
        }

        return validationErrors;
    }

    public void persist() throws DataAccessException {
        ProOperatorDAO dao = new ProOperatorDAO();
        ProOperator operator = new ProOperator.ProOperatorBuilder(
                getUsername(),
                getUserRealName(),
                getPassword(),
                getStatus()
        ).build();
        dao.save(operator);
        SloaneCommonDAO.insertUserInWebTables(getUsername(), getUserRealName(), getPassword());
    }
}
