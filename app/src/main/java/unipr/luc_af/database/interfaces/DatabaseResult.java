package unipr.luc_af.database.interfaces;

import android.database.Cursor;

@FunctionalInterface
public interface DatabaseResult {
    void OnResult(Cursor cursor);
}

