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
import dora.db.table.PrimaryKeyEntry;
import dora.db.table.Table;
import site.doramusic.app.sort.Sort;

/**
 * 歌手表。
 */
@Table("artist")
public class Artist implements OrmTable, Parcelable, Sort {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ARTIST_NAME = "artist_name";
    public static final String COLUMN_NUMBER_OF_TRACKS = "number_of_tracks";

    @Ignore
    private String sortLetter;

    @Column(COLUMN_ID)
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public int id;

    @Column(COLUMN_ARTIST_NAME)
    public String name;

    /**
     * 曲目数。
     */
    @Column(COLUMN_NUMBER_OF_TRACKS)
    public int number_of_tracks;

    @Override
    public int describeContents() {
        return 0;
    }


    public Artist() {
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt(COLUMN_ID, id);
        bundle.putString(COLUMN_ARTIST_NAME, name);
        bundle.putInt(COLUMN_NUMBER_OF_TRACKS, number_of_tracks);
        dest.writeBundle(bundle);
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {

        @Override
        public Artist createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            Artist artist = new Artist();
            artist.id = bundle.getInt(COLUMN_ID);
            artist.name = bundle.getString(COLUMN_ARTIST_NAME);
            artist.number_of_tracks = bundle.getInt(COLUMN_NUMBER_OF_TRACKS);
            return artist;
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @NonNull
    @Override
    public PrimaryKeyEntry getPrimaryKey() {
        return new PrimaryKeyEntry(COLUMN_ID, id);
    }

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

    public int compareTo(Sort sort) {
        return getSortLetter().compareTo(sort.getSortLetter());
    }

    @NonNull
    @Override
    public OrmMigration[] getMigrations() {
        return new OrmMigration[0];
    }
}
