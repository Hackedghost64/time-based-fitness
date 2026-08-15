package com.timebasedfitness.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.timebasedfitness.app.data.model.Category;
import com.timebasedfitness.app.data.model.CompletionLog;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.Instant;
import java.time.LocalDate;
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
public final class CompletionLogDao_Impl implements CompletionLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CompletionLog> __insertionAdapterOfCompletionLog;

  private final Converters __converters = new Converters();

  public CompletionLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCompletionLog = new EntityInsertionAdapter<CompletionLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `completion_logs` (`date`,`category`,`completedAt`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CompletionLog entity) {
        final long _tmp = __converters.fromLocalDate(entity.getDate());
        statement.bindLong(1, _tmp);
        final String _tmp_1 = __converters.fromCategory(entity.getCategory());
        statement.bindString(2, _tmp_1);
        final long _tmp_2 = __converters.fromInstant(entity.getCompletedAt());
        statement.bindLong(3, _tmp_2);
      }
    };
  }

  @Override
  public Object insertLog(final CompletionLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCompletionLog.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CompletionLog>> getAllLogs() {
    final String _sql = "SELECT * FROM completion_logs ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"completion_logs"}, new Callable<List<CompletionLog>>() {
      @Override
      @NonNull
      public List<CompletionLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<CompletionLog> _result = new ArrayList<CompletionLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionLog _item;
            final LocalDate _tmpDate;
            final long _tmp;
            _tmp = _cursor.getLong(_cursorIndexOfDate);
            _tmpDate = __converters.toLocalDate(_tmp);
            final Category _tmpCategory;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toCategory(_tmp_1);
            final Instant _tmpCompletedAt;
            final long _tmp_2;
            _tmp_2 = _cursor.getLong(_cursorIndexOfCompletedAt);
            _tmpCompletedAt = __converters.toInstant(_tmp_2);
            _item = new CompletionLog(_tmpDate,_tmpCategory,_tmpCompletedAt);
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
  public Object getAllLogsSync(final Continuation<? super List<CompletionLog>> $completion) {
    final String _sql = "SELECT * FROM completion_logs ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CompletionLog>>() {
      @Override
      @NonNull
      public List<CompletionLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<CompletionLog> _result = new ArrayList<CompletionLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionLog _item;
            final LocalDate _tmpDate;
            final long _tmp;
            _tmp = _cursor.getLong(_cursorIndexOfDate);
            _tmpDate = __converters.toLocalDate(_tmp);
            final Category _tmpCategory;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toCategory(_tmp_1);
            final Instant _tmpCompletedAt;
            final long _tmp_2;
            _tmp_2 = _cursor.getLong(_cursorIndexOfCompletedAt);
            _tmpCompletedAt = __converters.toInstant(_tmp_2);
            _item = new CompletionLog(_tmpDate,_tmpCategory,_tmpCompletedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
