package org.hildan.agenda.generator;

/**
 * This wrapping is necessary because docx-stamper's proxy system does not work inside comments.
 */
public class JContext {

    private Agenda a;

    public Agenda getA() {
        return a;
    }

    public void setA(Agenda a) {
        this.a = a;
    }
}

