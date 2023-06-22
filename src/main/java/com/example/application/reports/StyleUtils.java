package com.example.application.reports;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.conditionalStyle.ConditionalStyle;

public abstract class StyleUtils {
	public static Style backgroundColorStyle(String hexColor) {
		Style cellBackgroundStyle = new Style();
		cellBackgroundStyle.setTransparency(Transparency.OPAQUE);
		cellBackgroundStyle.setBackgroundColor(Color.decode(hexColor));
		cellBackgroundStyle.setTextColor(Color.BLACK);
		cellBackgroundStyle.setVerticalAlign(VerticalAlign.TOP);
		return cellBackgroundStyle;
	}

	public static ArrayList<ConditionalStyle> getConditonalStyles(List<String> Color) {
		ArrayList<ConditionalStyle> conditionalStyles = new ArrayList<ConditionalStyle>();
		Color.forEach(c -> {
			BackgroundCondition backgroundCondition = new BackgroundCondition("color", c);
			ConditionalStyle cs = new ConditionalStyle(backgroundCondition, StyleUtils.backgroundColorStyle(c));
			conditionalStyles.add(cs);

		});
		return conditionalStyles;
	}
}