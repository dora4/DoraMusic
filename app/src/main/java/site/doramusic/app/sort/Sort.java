package site.doramusic.app.sort;

/**
 * 按字母排序的接口，用于排序。
 */
public interface Sort extends Comparable<Sort> {

    /**
     * 设置用于排序的属性的字母。
     *
     * @param sortLetter 用于排序的属性的字母
     */
    void setSortLetter(String sortLetter);

    /**
     * 获取用于排序的属性的字母。
     *
     * @return 用于排序的属性的字母
     */
    String getSortLetter();
}
