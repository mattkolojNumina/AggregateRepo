package host.geek;

import dao.DataAccessException;
import dao.GeekUserDAO;
import dao.ProOperatorDAO;
import records.GeekUser;
import records.GeekUserRole;
import records.ProOperator;

import java.util.List;

import static sloane.SloaneConstants.geekWarehouseCode;
import static rds.RDSLog.alert;

public class OperatorGeekTranslator implements GeekTranslator {

    public void moveToGeek() {
        ProOperatorDAO dao = new ProOperatorDAO();
        GeekUserDAO geekUserDAO = new GeekUserDAO();

        try {
            List<ProOperator> operatorList = dao.getAll();
            for(ProOperator operator: operatorList) {
                GeekUser geekUser = new GeekUser.GeekUserBuilder(
                        geekWarehouseCode,
                        operator.getOperatorId(),
                        operator.getOperatorName(),
                        operator.getPassword(),
                        operator.getStatus()
                ).build();
                geekUserDAO.save(geekUser);
            }
            geekUserDAO.setProcessedFlag();
        } catch (DataAccessException e) {
            alert("Error occurred while moving an operator to RDS Geek geekUser. [%s]", e.toString());
        }
    }

    public static void main(String[] args) {
        GeekTranslator gk = new OperatorGeekTranslator();
        gk.moveToGeek();
    }

    @Override
    public void acknowledgedByGeek() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'acknowledgedByGeek'");
    }

    @Override
    public void moveFromGeek() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveFromGeek'");
    }
}
