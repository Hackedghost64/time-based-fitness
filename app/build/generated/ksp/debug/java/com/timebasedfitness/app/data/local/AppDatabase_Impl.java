package com.timebasedfitness.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CategorySelectionDao _categorySelectionDao;

  private volatile CompletionLogDao _completionLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `category_selections` (`category` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, PRIMARY KEY(`category`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `completion_logs` (`date` INTEGER NOT NULL, `category` TEXT NOT NULL, `completedAt` INTEGER NOT NULL, PRIMARY KEY(`date`, `category`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e3dc9fcdbaff44d8c90a6c70548faca8')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `category_selections`");
        db.execSQL("DROP TABLE IF EXISTS `completion_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCategorySelections = new HashMap<String, TableInfo.Column>(4);
        _columnsCategorySelections.put("category", new TableInfo.Column("category", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategorySelections.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategorySelections.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategorySelections.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategorySelections = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategorySelections = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategorySelections = new TableInfo("category_selections", _columnsCategorySelections, _foreignKeysCategorySelections, _indicesCategorySelections);
        final TableInfo _existingCategorySelections = TableInfo.read(db, "category_selections");
        if (!_infoCategorySelections.equals(_existingCategorySelections)) {
          return new RoomOpenHelper.ValidationResult(false, "category_selections(com.timebasedfitness.app.data.model.CategorySelection).\n"
                  + " Expected:\n" + _infoCategorySelections + "\n"
                  + " Found:\n" + _existingCategorySelections);
        }
        final HashMap<String, TableInfo.Column> _columnsCompletionLogs = new HashMap<String, TableInfo.Column>(3);
        _columnsCompletionLogs.put("date", new TableInfo.Column("date", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCompletionLogs.put("category", new TableInfo.Column("category", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCompletionLogs.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCompletionLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCompletionLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCompletionLogs = new TableInfo("completion_logs", _columnsCompletionLogs, _foreignKeysCompletionLogs, _indicesCompletionLogs);
        final TableInfo _existingCompletionLogs = TableInfo.read(db, "completion_logs");
        if (!_infoCompletionLogs.equals(_existingCompletionLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "completion_logs(com.timebasedfitness.app.data.model.CompletionLog).\n"
                  + " Expected:\n" + _infoCompletionLogs + "\n"
                  + " Found:\n" + _existingCompletionLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e3dc9fcdbaff44d8c90a6c70548faca8", "8bb4208d9d2ae90b7e6dfff1af3d9e81");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "category_selections","completion_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `category_selections`");
      _db.execSQL("DELETE FROM `completion_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CategorySelectionDao.class, CategorySelectionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CompletionLogDao.class, CompletionLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CategorySelectionDao categorySelectionDao() {
    if (_categorySelectionDao != null) {
      return _categorySelectionDao;
    } else {
      synchronized(this) {
        if(_categorySelectionDao == null) {
          _categorySelectionDao = new CategorySelectionDao_Impl(this);
        }
        return _categorySelectionDao;
      }
    }
  }

  @Override
  public CompletionLogDao completionLogDao() {
    if (_completionLogDao != null) {
      return _completionLogDao;
    } else {
      synchronized(this) {
        if(_completionLogDao == null) {
          _completionLogDao = new CompletionLogDao_Impl(this);
        }
        return _completionLogDao;
      }
    }
  }
}
