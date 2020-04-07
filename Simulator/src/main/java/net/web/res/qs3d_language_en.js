'use strict';

var Language;

if (typeof Language=='undefined') {

Language={};

Language.xmlModelRoot="Model";
Language.xmlModelName="ModelName";
Language.xmlModelElements="ModelElements"; 
Language.xmlId="id";
Language.xmlModelElementSize="ModelElementSize";
Language.xmlModelElementName="ModelElementName";
Language.xmlModelElementConnection="ModelElementConnection";
Language.xmlModelElementConnectionElement1="Element1";
Language.xmlModelElementConnectionElement2="Element2";
Language.xmlModelElementText="ModelElementText";
Language.xmlModelElementTextLine="ModelElementTextLine";
Language.xmlModelElementFontSize="ModelElementFontSize";
Language.xmlModelElementStartsWith="ModelElement";
Language.xmlModelElementAnimationStartsWith="ModelElementAnimation";
Language.xmlModelElementVertex="ModelElementVertex";
Language.xmlModelElementEllipse="ModelElementEllipse";
Language.xmlModelElementLine="ModelElementLine";
Language.xmlModelElementRectangle="ModelElementRectangle";

Language.info1="Cursor: movement, Shift+Cursor: fast movement, Ctrl+Cursor: rotate and altitude";
Language.info2="Pressed left mouse button: rotate and altitude, Mouse wheel: movement, Shift+mouse wheel: fast movement, Ctrl+mouse wheel: altitude";
Language.info3="0: start position, page up/down: direction of view up/down, Ende: direction of view to front";
Language.infoCamera="camera";
Language.infoHide="hide";

Language.play="Play";
Language.pause="Pause";

}