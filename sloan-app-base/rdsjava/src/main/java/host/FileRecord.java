package host;

import dao.DataAccessException;

import java.util.List;

public interface FileRecord {
    List<String> validate() throws NumberFormatException, DataAccessException;

    void persist() throws DataAccessException;
}
