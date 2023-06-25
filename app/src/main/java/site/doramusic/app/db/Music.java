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
 * 歌曲表。
 */
@Table("music")
public class Music implements OrmTable, Parcelable, Sort {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SONG_ID = "song_id";
    public static final String COLUMN_ALBUM_ID = "album_id";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_MUSIC_NAME = "music_name";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_FOLDER = "folder";
    public static final String COLUMN_MUSIC_NAME_KEY = "music_name_key";
    public static final String COLUMN_ARTIST_KEY = "artist_key";
    public static final String COLUMN_FAVORITE = "favorite";
    public static final String COLUMN_LAST_PLAY_TIME = "last_play_time";

    /**
     * 数据库中的_id
     */
    @Column(COLUMN_ID)
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public int id;
    @Column(COLUMN_SONG_ID)
    public int songId = -1;
    @Column(COLUMN_ALBUM_ID)
    public int albumId = -1;
    @Column(COLUMN_DURATION)
    public int duration;
    @Column(COLUMN_MUSIC_NAME)
    public String musicName;
    @Column(COLUMN_ARTIST)
    public String artist;
    @Column(COLUMN_DATA)
    public String data;
    @Column(COLUMN_FOLDER)
    public String folder;
    @Column(COLUMN_MUSIC_NAME_KEY)
    public String musicNameKey;
    @Column(COLUMN_ARTIST_KEY)
    public String artistKey;
    @Column(COLUMN_FAVORITE)
    public int favorite;
    @Column(COLUMN_LAST_PLAY_TIME)
    public long lastPlayTime;
    @Ignore
    private String sortLetter;
    @Ignore
    private Type type;

    /**
     * 封面路径，在线歌曲用。
     */
    @Ignore
    private String coverPath;

    @NonNull
    @Override
    public OrmMigration[] getMigrations() {
        return new OrmMigration[0];
    }

    public enum  Type {
        LOCAL, ONLINE
    }

    public Music() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt(COLUMN_ID, id);
        bundle.putInt(COLUMN_SONG_ID, songId);
        bundle.putInt(COLUMN_ALBUM_ID, albumId);
        bundle.putInt(COLUMN_DURATION, duration);
        bundle.putString(COLUMN_MUSIC_NAME, musicName);
        bundle.putString(COLUMN_ARTIST, artist);
        bundle.putString(COLUMN_DATA, data);
        bundle.putString(COLUMN_FOLDER, folder);
        bundle.putString(COLUMN_MUSIC_NAME_KEY, musicNameKey);
        bundle.putString(COLUMN_ARTIST_KEY, artistKey);
        bundle.putInt(COLUMN_FAVORITE, favorite);
        bundle.putLong(COLUMN_LAST_PLAY_TIME, lastPlayTime);
        dest.writeBundle(bundle);
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {

        @Override
        public Music createFromParcel(Parcel source) {
            Music music = new Music();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            music.id = bundle.getInt(COLUMN_ID);
            music.songId = bundle.getInt(COLUMN_SONG_ID);
            music.albumId = bundle.getInt(COLUMN_ALBUM_ID);
            music.duration = bundle.getInt(COLUMN_DURATION);
            music.musicName = bundle.getString(COLUMN_MUSIC_NAME);
            music.artist = bundle.getString(COLUMN_ARTIST);
            music.data = bundle.getString(COLUMN_DATA);
            music.folder = bundle.getString(COLUMN_FOLDER);
            music.musicNameKey = bundle.getString(COLUMN_MUSIC_NAME_KEY);
            music.artistKey = bundle.getString(COLUMN_ARTIST_KEY);
            music.favorite = bundle.getInt(COLUMN_FAVORITE);
            music.lastPlayTime = bundle.getLong(COLUMN_LAST_PLAY_TIME);
            return music;
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public String toString() {
        return "DoraMusic{" +
                "id=" + id +
                ", songId=" + songId +
                ", albumId=" + albumId +
                ", duration=" + duration +
                ", musicName='" + musicName + '\'' +
                ", artist='" + artist + '\'' +
                ", data='" + data + '\'' +
                ", folder='" + folder + '\'' +
                ", musicNameKey='" + musicNameKey + '\'' +
                ", artistKey='" + artistKey + '\'' +
                ", favorite=" + favorite +
                ", lastPlayTime=" + lastPlayTime +
                '}';
    }

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

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getCoverPath() {
        return coverPath;
    }

    @Override
    public int compareTo(Sort sort) {
        return getSortLetter().compareTo(sort.getSortLetter());
    }
}
