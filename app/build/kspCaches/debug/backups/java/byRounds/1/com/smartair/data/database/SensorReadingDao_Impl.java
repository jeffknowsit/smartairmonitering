package com.smartair.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.smartair.data.model.AirStatus;
import com.smartair.data.model.SensorReading;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SensorReadingDao_Impl implements SensorReadingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SensorReading> __insertionAdapterOfSensorReading;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  public SensorReadingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSensorReading = new EntityInsertionAdapter<SensorReading>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sensor_readings` (`id`,`timestamp`,`temperature`,`humidity`,`dust`,`gas`,`fan`,`buzzer`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SensorReading entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        if (entity.getTemperature() == null) {
          statement.bindNull(3);
        } else {
          statement.bindDouble(3, entity.getTemperature());
        }
        if (entity.getHumidity() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getHumidity());
        }
        if (entity.getDust() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getDust());
        }
        if (entity.getGas() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getGas());
        }
        final int _tmp = entity.getFan() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getBuzzer() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final String _tmp_2 = __converters.fromAirStatus(entity.getStatus());
        statement.bindString(9, _tmp_2);
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sensor_readings WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SensorReading reading, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSensorReading.insertAndReturnId(reading);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOlderThan(final long before, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, before);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<SensorReading> getLatestReading() {
    final String _sql = "SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sensor_readings"}, new Callable<SensorReading>() {
      @Override
      @Nullable
      public SensorReading call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfDust = CursorUtil.getColumnIndexOrThrow(_cursor, "dust");
          final int _cursorIndexOfGas = CursorUtil.getColumnIndexOrThrow(_cursor, "gas");
          final int _cursorIndexOfFan = CursorUtil.getColumnIndexOrThrow(_cursor, "fan");
          final int _cursorIndexOfBuzzer = CursorUtil.getColumnIndexOrThrow(_cursor, "buzzer");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final SensorReading _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Float _tmpTemperature;
            if (_cursor.isNull(_cursorIndexOfTemperature)) {
              _tmpTemperature = null;
            } else {
              _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            }
            final Float _tmpHumidity;
            if (_cursor.isNull(_cursorIndexOfHumidity)) {
              _tmpHumidity = null;
            } else {
              _tmpHumidity = _cursor.getFloat(_cursorIndexOfHumidity);
            }
            final Integer _tmpDust;
            if (_cursor.isNull(_cursorIndexOfDust)) {
              _tmpDust = null;
            } else {
              _tmpDust = _cursor.getInt(_cursorIndexOfDust);
            }
            final Integer _tmpGas;
            if (_cursor.isNull(_cursorIndexOfGas)) {
              _tmpGas = null;
            } else {
              _tmpGas = _cursor.getInt(_cursorIndexOfGas);
            }
            final boolean _tmpFan;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFan);
            _tmpFan = _tmp != 0;
            final boolean _tmpBuzzer;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBuzzer);
            _tmpBuzzer = _tmp_1 != 0;
            final AirStatus _tmpStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAirStatus(_tmp_2);
            _result = new SensorReading(_tmpId,_tmpTimestamp,_tmpTemperature,_tmpHumidity,_tmpDust,_tmpGas,_tmpFan,_tmpBuzzer,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SensorReading>> getReadingsSince(final long since) {
    final String _sql = "SELECT * FROM sensor_readings WHERE timestamp > ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sensor_readings"}, new Callable<List<SensorReading>>() {
      @Override
      @NonNull
      public List<SensorReading> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfDust = CursorUtil.getColumnIndexOrThrow(_cursor, "dust");
          final int _cursorIndexOfGas = CursorUtil.getColumnIndexOrThrow(_cursor, "gas");
          final int _cursorIndexOfFan = CursorUtil.getColumnIndexOrThrow(_cursor, "fan");
          final int _cursorIndexOfBuzzer = CursorUtil.getColumnIndexOrThrow(_cursor, "buzzer");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<SensorReading> _result = new ArrayList<SensorReading>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SensorReading _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Float _tmpTemperature;
            if (_cursor.isNull(_cursorIndexOfTemperature)) {
              _tmpTemperature = null;
            } else {
              _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            }
            final Float _tmpHumidity;
            if (_cursor.isNull(_cursorIndexOfHumidity)) {
              _tmpHumidity = null;
            } else {
              _tmpHumidity = _cursor.getFloat(_cursorIndexOfHumidity);
            }
            final Integer _tmpDust;
            if (_cursor.isNull(_cursorIndexOfDust)) {
              _tmpDust = null;
            } else {
              _tmpDust = _cursor.getInt(_cursorIndexOfDust);
            }
            final Integer _tmpGas;
            if (_cursor.isNull(_cursorIndexOfGas)) {
              _tmpGas = null;
            } else {
              _tmpGas = _cursor.getInt(_cursorIndexOfGas);
            }
            final boolean _tmpFan;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFan);
            _tmpFan = _tmp != 0;
            final boolean _tmpBuzzer;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBuzzer);
            _tmpBuzzer = _tmp_1 != 0;
            final AirStatus _tmpStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAirStatus(_tmp_2);
            _item = new SensorReading(_tmpId,_tmpTimestamp,_tmpTemperature,_tmpHumidity,_tmpDust,_tmpGas,_tmpFan,_tmpBuzzer,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getReadingsSinceSnapshot(final long since,
      final Continuation<? super List<SensorReading>> $completion) {
    final String _sql = "SELECT * FROM sensor_readings WHERE timestamp > ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SensorReading>>() {
      @Override
      @NonNull
      public List<SensorReading> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfDust = CursorUtil.getColumnIndexOrThrow(_cursor, "dust");
          final int _cursorIndexOfGas = CursorUtil.getColumnIndexOrThrow(_cursor, "gas");
          final int _cursorIndexOfFan = CursorUtil.getColumnIndexOrThrow(_cursor, "fan");
          final int _cursorIndexOfBuzzer = CursorUtil.getColumnIndexOrThrow(_cursor, "buzzer");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<SensorReading> _result = new ArrayList<SensorReading>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SensorReading _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Float _tmpTemperature;
            if (_cursor.isNull(_cursorIndexOfTemperature)) {
              _tmpTemperature = null;
            } else {
              _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            }
            final Float _tmpHumidity;
            if (_cursor.isNull(_cursorIndexOfHumidity)) {
              _tmpHumidity = null;
            } else {
              _tmpHumidity = _cursor.getFloat(_cursorIndexOfHumidity);
            }
            final Integer _tmpDust;
            if (_cursor.isNull(_cursorIndexOfDust)) {
              _tmpDust = null;
            } else {
              _tmpDust = _cursor.getInt(_cursorIndexOfDust);
            }
            final Integer _tmpGas;
            if (_cursor.isNull(_cursorIndexOfGas)) {
              _tmpGas = null;
            } else {
              _tmpGas = _cursor.getInt(_cursorIndexOfGas);
            }
            final boolean _tmpFan;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFan);
            _tmpFan = _tmp != 0;
            final boolean _tmpBuzzer;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBuzzer);
            _tmpBuzzer = _tmp_1 != 0;
            final AirStatus _tmpStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAirStatus(_tmp_2);
            _item = new SensorReading(_tmpId,_tmpTimestamp,_tmpTemperature,_tmpHumidity,_tmpDust,_tmpGas,_tmpFan,_tmpBuzzer,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentReadings(final int limit,
      final Continuation<? super List<SensorReading>> $completion) {
    final String _sql = "SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SensorReading>>() {
      @Override
      @NonNull
      public List<SensorReading> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfDust = CursorUtil.getColumnIndexOrThrow(_cursor, "dust");
          final int _cursorIndexOfGas = CursorUtil.getColumnIndexOrThrow(_cursor, "gas");
          final int _cursorIndexOfFan = CursorUtil.getColumnIndexOrThrow(_cursor, "fan");
          final int _cursorIndexOfBuzzer = CursorUtil.getColumnIndexOrThrow(_cursor, "buzzer");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<SensorReading> _result = new ArrayList<SensorReading>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SensorReading _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Float _tmpTemperature;
            if (_cursor.isNull(_cursorIndexOfTemperature)) {
              _tmpTemperature = null;
            } else {
              _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            }
            final Float _tmpHumidity;
            if (_cursor.isNull(_cursorIndexOfHumidity)) {
              _tmpHumidity = null;
            } else {
              _tmpHumidity = _cursor.getFloat(_cursorIndexOfHumidity);
            }
            final Integer _tmpDust;
            if (_cursor.isNull(_cursorIndexOfDust)) {
              _tmpDust = null;
            } else {
              _tmpDust = _cursor.getInt(_cursorIndexOfDust);
            }
            final Integer _tmpGas;
            if (_cursor.isNull(_cursorIndexOfGas)) {
              _tmpGas = null;
            } else {
              _tmpGas = _cursor.getInt(_cursorIndexOfGas);
            }
            final boolean _tmpFan;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFan);
            _tmpFan = _tmp != 0;
            final boolean _tmpBuzzer;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBuzzer);
            _tmpBuzzer = _tmp_1 != 0;
            final AirStatus _tmpStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAirStatus(_tmp_2);
            _item = new SensorReading(_tmpId,_tmpTimestamp,_tmpTemperature,_tmpHumidity,_tmpDust,_tmpGas,_tmpFan,_tmpBuzzer,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWarningCountSince(final long since,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sensor_readings WHERE timestamp > ? AND status != 'NORMAL'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAvgTemperatureSince(final long since,
      final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(temperature) FROM sensor_readings WHERE timestamp > ? AND temperature IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAvgHumiditySince(final long since,
      final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(humidity) FROM sensor_readings WHERE timestamp > ? AND humidity IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAvgDustSince(final long since, final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(dust) FROM sensor_readings WHERE timestamp > ? AND dust IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAvgGasSince(final long since, final Continuation<? super Float> $completion) {
    final String _sql = "SELECT AVG(gas) FROM sensor_readings WHERE timestamp > ? AND gas IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
