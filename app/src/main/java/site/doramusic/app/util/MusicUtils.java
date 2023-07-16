package site.doramusic.app.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class MusicUtils {

    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final HashMap<Long, Bitmap> sArtCache = new HashMap<>();
    private static final Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    static {
        // for the cache,
        // 565 is faster to decode and display
        // and we don't want to dither here because the image will be scaled
        // down later
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptionsCache.inDither = false;

        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptions.inDither = false;
    }

    public static String formatTime(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }

    public static Bitmap getCachedArtwork(Context context, long artIndex,
                                          Bitmap defaultArtwork) {
        Bitmap bitmap = null;
        synchronized (sArtCache) {
            bitmap = sArtCache.get(artIndex);
        }
        if (context == null) {
            return null;
        }
        if (bitmap == null) {
            bitmap = defaultArtwork;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
            if (b != null) {
                bitmap = b;
                synchronized (sArtCache) {
                    // the cache may have changed since we checked
                    Bitmap value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, bitmap);
                    } else {
                        bitmap = value;
                    }
                }
            }
        }
        return bitmap;
    }

    // A really simple BitmapDrawable-like class, that doesn't do
    // scaling, dithering or filtering.
    /*
     * private static class FastBitmapDrawable extends Drawable { private Bitmap
     * mBitmap; public FastBitmapDrawable(Bitmap b) { mBitmap = b; }
     *
     * @Override public void draw(Canvas canvas) { canvas.drawBitmap(mBitmap, 0,
     * 0, null); }
     *
     * @Override public int getOpacity() { return PixelFormat.OPAQUE; }
     *
     * @Override public void setAlpha(int alpha) { }
     *
     * @Override public void setColorFilter(ColorFilter cf) { } }
     */

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    public static Bitmap getArtworkQuick(Context context, long album_id, int w,
                                         int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;
                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
                        null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }
                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w
                            || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same
                        // bitmap
                        if (tmp != b)
                            b.recycle();
                        b = tmp;
                    }
                }
                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }


    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead) This method always returns
     * the default album art icon when no album art is found.
     */
    /*
     * public static Bitmap getArtwork(Context context, long song_id, long
     * album_id) { return getArtwork(context, song_id, album_id, true); }
     */

    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    /*
     * public static Bitmap getArtwork(Context context, long song_id, long
     * album_id, boolean allowdefault) {
     *
     * // This is something that is not in the database, so get the album // art
     * directly // from the file. if (song_id >= 0) { Bitmap bm =
     * getArtworkFromFile(context, song_id, -1); if (bm != null) { return bm; }
     * else { return getArtwork(context, -1, album_id); } } else if (album_id >=
     * 0) {
     *
     * ContentResolver res = context.getContentResolver(); Uri uri =
     * ContentUris.withAppendedId(sArtworkUri, album_id); if (uri != null) {
     * InputStream in = null; try { in = res.openInputStream(uri); return
     * BitmapFactory.decodeStream(in, null, sBitmapOptions); } catch
     * (FileNotFoundException ex) { // The album art thumbnail does not actually
     * exist. Maybe // the // user deleted it, or // maybe it never existed to
     * begin with. Bitmap bm = getArtworkFromFile(context, song_id, album_id);
     * if (bm != null) { if (bm.getConfig() == null) { bm =
     * bm.copy(Bitmap.Config.RGB_565, false); if (bm == null && allowdefault) {
     * return getDefaultArtwork(context); } } } else if (allowdefault) { bm =
     * getDefaultArtwork(context); } return bm; } finally { try { if (in !=
     * null) { in.close(); } } catch (IOException ex) { } } }
     *
     * }
     *
     * return null; }
     *
     * // get album art for specified file private static final String
     * sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
     * .toString(); private static Bitmap mCachedBit = null;
     *
     * private static Bitmap getArtworkFromFile(Context context, long songid,
     * long albumid) { Bitmap bm = null; byte[] art = null; String path = null;
     *
     * if (albumid < 0 && songid < 0) { throw new IllegalArgumentException(
     * "Must specify an album or a song id"); }
     *
     * try { if (songid >= 0) { Uri uri =
     * Uri.parse("content://media/external/audio/media/" + songid +
     * "/albumart"); ParcelFileDescriptor pfd = context.getContentResolver()
     * .openFileDescriptor(uri, "r"); if (pfd != null) { FileDescriptor fd =
     * pfd.getFileDescriptor(); bm = BitmapFactory.decodeFileDescriptor(fd); }
     * else { return getArtworkFromFile(context, -1, albumid); } } else if
     * (albumid >= 0) { Uri uri = ContentUris.withAppendedId(sArtworkUri,
     * albumid); ParcelFileDescriptor pfd = context.getContentResolver()
     * .openFileDescriptor(uri, "r"); if (pfd != null) { FileDescriptor fd =
     * pfd.getFileDescriptor(); bm = BitmapFactory.decodeFileDescriptor(fd); } }
     * } catch (IllegalStateException ex) { } catch (FileNotFoundException ex) {
     * } if (bm != null) { mCachedBit = bm; } return bm; }
     *
     * private static Bitmap getDefaultArtwork(Context context) {
     * BitmapFactory.Options opts = new BitmapFactory.Options();
     * opts.inPreferredConfig = Bitmap.Config.ARGB_8888; return
     * BitmapFactory.decodeStream(context.getResources()
     * .openRawResource(R.drawable.img_album_background), null, opts); }
     */
    public static void clearCache() {
        sArtCache.clear();
    }

    public static String bytesToMB(long bytes) {
        float size = (float) (bytes * 1.0 / 1024 / 1024);
        String result = null;
        if (bytes >= (1024 * 1024)) {
            result = new DecimalFormat("###.00").format(size) + "MB";
        } else {
            result = new DecimalFormat("0.00").format(size) + "MB";
        }
        return result;
    }


    public static int getDuration(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        return Integer.valueOf(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION));
    }
}
