package org.lostcitymapeditor.OriginalCode;

public class Linkable {

    public long key;

    public Linkable next;

    public Linkable prev;

    public final void unlink() {
        if (this.prev != null) {
            this.prev.next = this.next;
            this.next.prev = this.prev;
            this.next = null;
            this.prev = null;
        }
    }
}
