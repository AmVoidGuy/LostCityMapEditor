package org.lostcitymapeditor.OriginalCode;


public class DoublyLinkable extends Linkable {

    public DoublyLinkable next2;

    public DoublyLinkable prev2;

    public final void uncache() {
        if (this.prev2 != null) {
            this.prev2.next2 = this.next2;
            this.next2.prev2 = this.prev2;
            this.next2 = null;
            this.prev2 = null;
        }
    }
}
