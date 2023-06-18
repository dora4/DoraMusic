package site.doramusic.app.sort;

public interface Sort extends Comparable<Sort> {
    void setSortLetter(String sortLetter);
    String getSortLetter();
}
