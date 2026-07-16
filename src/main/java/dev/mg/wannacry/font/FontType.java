package dev.mg.wannacry.font;

public enum FontType {
    ARIAL("arial.ttf"),
    ARIALBD("arialbd.ttf"),
    ARIALBI("arialbi.ttf"),
    ARIALI("ariali.ttf"),
    ARIALBLK("arialblk.ttf"),
    VERDANA("verdana.ttf"),
    VERDANAB("verdanab.ttf"),
    VERDANAI("verdanai.ttf"),
    VERDANAZ("verdanaz.ttf"),
    QUICKSAND("quicksand.ttf"),
    QUICKSAND_BOLD("quicksandb.ttf"),
    QUICKSAND_LIGHT("quicksandl.ttf"),
    QUICKSAND_MEDIUM("quicksandm.ttf"),
    QUICKSAND_SEMIBOLD("quicksandsb.ttf"),
    LEXEND_DECA("lexend.ttf"),
    LEXEND_DECA_THIN("lexendt.ttf"),
    LEXEND_DECA_EXTRALIGHT("lexendel.ttf"),
    LEXEND_DECA_LIGHT("lexendl.ttf"),
    LEXEND_DECA_MEDIUM("lexendm.ttf"),
    LEXEND_DECA_SEMIBOLD("lexendsb.ttf"),
    LEXEND_DECA_BOLD("lexendb.ttf"),
    LEXEND_DECA_EXTRABOLD("lexendeb.ttf"),
    LEXEND_DECA_BLACK("lexendblk.ttf");

    private final String fileName;

    FontType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
