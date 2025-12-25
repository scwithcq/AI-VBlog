package com.vblog.common;

import java.util.Collections;
import java.util.List;

/**
 * Custom Page Class for pagination
 * 
 * @param <T> the type of the content of this page
 * @author V-Blog Team
 */
public class Page<T> {

    private long total;
    private int current;
    private int size;
    private List<T> records;

    /**
     * Default constructor
     */
    public Page() {
        this.current = 1;
        this.size = 10;
        this.records = Collections.emptyList();
        this.total = 0;
    }

    /**
     * Constructor
     *
     * @param current current page number
     * @param size    page size
     */
    public Page(int current, int size) {
        this.current = current;
        this.size = size;
        this.records = Collections.emptyList();
        this.total = 0;
    }

    /**
     * Constructor
     *
     * @param current current page number
     * @param size    page size
     * @param total   total records
     * @param records data records
     */
    public Page(int current, int size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getPages() {
        if (size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    public boolean hasPrevious() {
        return current > 1;
    }

    public boolean hasNext() {
        return current < getPages();
    }

    public int getOffset() {
        return (current - 1) * size;
    }

    public int getLimit() {
        return size;
    }

    @Override
    public String toString() {
        return "Page{" +
                "total=" + total +
                ", current=" + current +
                ", size=" + size +
                ", pages=" + getPages() +
                ", records=" + records.size() +
                '}';
    }

}
