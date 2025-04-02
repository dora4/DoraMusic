package site.doramusic.app.sort;

public interface Sort extends Comparable<Sort> {

    /**
     * 设置用于排序的属性的字母。
     *
     * @param sortLetter
     */
    void setSortLetter(String sortLetter);

    String getSortLetter();
}
