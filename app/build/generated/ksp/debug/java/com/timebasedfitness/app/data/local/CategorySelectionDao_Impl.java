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
import com.timebasedfitness.app.data.model.CategorySelection;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.LocalTime;
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
public final class CategorySelectionDao_Impl implements CategorySelectionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CategorySelection> __insertionAdapterOfCategorySelection;

  private final Converters __converters = new Converters();

  public CategorySelectionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCategorySelection = new EntityInsertionAdapter<CategorySelection>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `category_selections` (`category`,`isEnabled`,`startTime`,`endTime`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategorySelection entity) {
        final String _tmp = __converters.fromCategory(entity.getCategory());
        statement.bindString(1, _tmp);
        final int _tmp_1 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(2, _tmp_1);
        final int _tmp_2 = __converters.fromLocalTime(entity.getStartTime());
        statement.bindLong(3, _tmp_2);
        final int _tmp_3 = __converters.fromLocalTime(entity.getEndTime());
        statement.bindLong(4, _tmp_3);
      }
    };
  }

  @Override
  public Object insertAll(final List<CategorySelection> selections,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCategorySelection.insert(selections);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final CategorySelection selection,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCategorySelection.insert(selection);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CategorySelection>> getAllCategorySelections() {
    final String _sql = "SELECT * FROM category_selections";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"category_selections"}, new Callable<List<CategorySelection>>() {
      @Override
      @NonNull
      public List<CategorySelection> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final List<CategorySelection> _result = new ArrayList<CategorySelection>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategorySelection _item;
            final Category _tmpCategory;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toCategory(_tmp);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final LocalTime _tmpStartTime;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfStartTime);
            _tmpStartTime = __converters.toLocalTime(_tmp_2);
            final LocalTime _tmpEndTime;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfEndTime);
            _tmpEndTime = __converters.toLocalTime(_tmp_3);
            _item = new CategorySelection(_tmpCategory,_tmpIsEnabled,_tmpStartTime,_tmpEndTime);
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
  public Object getAllCategorySelectionsSync(
      final Continuation<? super List<CategorySelection>> $completion) {
    final String _sql = "SELECT * FROM category_selections";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CategorySelection>>() {
      @Override
      @NonNull
      public List<CategorySelection> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final List<CategorySelection> _result = new ArrayList<CategorySelection>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategorySelection _item;
            final Category _tmpCategory;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toCategory(_tmp);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final LocalTime _tmpStartTime;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfStartTime);
            _tmpStartTime = __converters.toLocalTime(_tmp_2);
            final LocalTime _tmpEndTime;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfEndTime);
            _tmpEndTime = __converters.toLocalTime(_tmp_3);
            _item = new CategorySelection(_tmpCategory,_tmpIsEnabled,_tmpStartTime,_tmpEndTime);
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
