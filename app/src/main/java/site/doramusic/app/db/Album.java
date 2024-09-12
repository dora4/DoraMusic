package site.doramusic.app.db;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import dora.db.constraint.AssignType;
import dora.db.constraint.PrimaryKey;
import dora.db.migration.OrmMigration;
import dora.db.table.Column;
import dora.db.table.Ignore;
import dora.db.table.OrmTable;
import dora.db.table.Table;
import site.doramusic.app.sort.Sort;

/**
 * 专辑表。
 */
@Table("album")
public class Album implements OrmTable, Parcelable, Sort {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ALBUM_NAME = "album_name";
    public static final String COLUMN_ALBUM_ID = "album_id";
    public static final String COLUMN_NUMBER_OF_SONGS = "number_of_songs";
    public static final String COLUMN_ALBUM_COVER_PATH = "album_cover_path";

    @Column(COLUMN_ID)
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public int id;

    @Ignore
    private String sortLetter;

    // 专辑名称
    @Column(COLUMN_ALBUM_NAME)
    public String album_name;

    // 专辑在数据库中的id
    @Column(COLUMN_ALBUM_ID)
    public int album_id = -1;

    // 专辑的歌曲数目
    @Column(COLUMN_NUMBER_OF_SONGS)
    public int number_of_songs = 0;

    // 专辑封面图片路径
    @Column(COLUMN_ALBUM_COVER_PATH)
    public String album_cover_path;

    @Override
    public int describeContents() {
        return 0;
    }


    public Album() {
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt(COLUMN_ID, id);
        bundle.putString(COLUMN_ALBUM_NAME, album_name);
        bundle.putString(COLUMN_ALBUM_COVER_PATH, album_cover_path);
        bundle.putInt(COLUMN_NUMBER_OF_SONGS, number_of_songs);
        bundle.putInt(COLUMN_ALBUM_ID, album_id);
        dest.writeBundle(bundle);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {

        @Override
        public Album createFromParcel(Parcel source) {
            Album album = new Album();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            album.id = bundle.getInt(COLUMN_ID);
            album.album_name = bundle.getString(COLUMN_ALBUM_NAME);
            album.album_cover_path = bundle.getString(COLUMN_ALBUM_COVER_PATH);
            album.number_of_songs = bundle.getInt(COLUMN_NUMBER_OF_SONGS);
            album.album_id = bundle.getInt(COLUMN_ALBUM_ID);
            return album;
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public boolean isUpgradeRecreated() {
        return false;
    }

    @Override
    public String getSortLetter() {
        return sortLetter;
    }

    @Override
    public void setSortLetter(String sortLetter) {
        this.sortLetter = sortLetter;
    }

    @Override
    public int compareTo(Sort sort) {
        return getSortLetter().compareTo(sort.getSortLetter());
    }

    @NonNull
    @Override
    public OrmMigration[] getMigrations() {
        return new OrmMigration[0];
    }
}
