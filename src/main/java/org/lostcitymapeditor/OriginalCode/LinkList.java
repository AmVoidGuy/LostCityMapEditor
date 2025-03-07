package org.lostcitymapeditor.OriginalCode;

public class LinkList {

    private final Linkable sentinel = new Linkable();
    private Linkable cursor;

    public LinkList() {
        this.sentinel.next = this.sentinel;
        this.sentinel.prev = this.sentinel;
    }

    public void addTail( Linkable node) {
        if (node.prev != null) {
            node.unlink();
        }

        node.prev = this.sentinel.prev;
        node.next = this.sentinel;
        node.prev.next = node;
        node.next.prev = node;
    }

    public void addHead( Linkable node) {
        if (node.prev != null) {
            node.unlink();
        }

        node.prev = this.sentinel;
        node.next = this.sentinel.next;
        node.prev.next = node;
        node.next.prev = node;
    }

    public Linkable removeHead() {
        Linkable node = this.sentinel.next;
        if (node == this.sentinel) {
            return null;
        }
        node.unlink();
        return node;
    }

    public Linkable head() {
        Linkable node = this.sentinel.next;
        if (node == this.sentinel) {
            this.cursor = null;
            return null;
        }
        this.cursor = node.next;
        return node;
    }

    public Linkable tail() {
        Linkable node = this.sentinel.prev;
        if (node == this.sentinel) {
            this.cursor = null;
            return null;
        }
        this.cursor = node.prev;
        return node;
    }

    public Linkable next() {
        Linkable node = this.cursor;
        if (node == this.sentinel) {
            this.cursor = null;
            return null;
        }
        this.cursor = node.next;
        return node;
    }

    public Linkable prev() {
        Linkable node = this.cursor;
        if (node == this.sentinel) {
            this.cursor = null;
            return null;
        }
        this.cursor = node.prev;
        return node;
    }

    public void clear() {
        while (true) {
            Linkable node = this.sentinel.next;
            if (node == this.sentinel) {
                return;
            }
            node.unlink();
        }
    }
}
