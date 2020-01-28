package unipr.luc_af.database.interfaces;

import android.database.SQLException;

@FunctionalInterface
public interface DatabaseError {
    void OnError(SQLException exception);
}
