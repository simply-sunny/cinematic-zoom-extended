package mix.cinematiczoom;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Native settings editor for Forge versions without a compatible Cloth Config release. */
public final class ZoomSettingsScreen extends Screen {
    private final Screen parent;
    private final List<Field> fields = Arrays.stream(ZoomConfig.class.getFields())
        .filter(f -> !Modifier.isStatic(f.getModifiers()) && !f.getName().equals("configVersion") && !f.getName().equals("maxZoomMultiplier")).toList();
    private final Map<Field, String> values = new LinkedHashMap<>();
    private final Map<Field, EditBox> editors = new LinkedHashMap<>();
    private int page, pageSize;
    private String error = "";

    public static Screen create(Screen parent) { return new ZoomSettingsScreen(parent); }
    private ZoomSettingsScreen(Screen parent) {
        super(Component.literal("Cinematic Zoom Extended")); this.parent = parent;
        for (Field field : fields) {
            try {
                Object value = field.get(ZoomConfig.INSTANCE);
                if (field.getName().equals("minZoomMultiplier")) value = Math.round(1.0 / ((Number)value).doubleValue() * 1000) / 1000.0;
                values.put(field, value instanceof List<?> list ? String.join(", ", list.stream().map(Object::toString).toList()) : value.toString());
            } catch (IllegalAccessException e) { throw new IllegalStateException(e); }
        }
    }
    private static String label(Field field) {
        if (field.getName().equals("minZoomMultiplier")) return "Maximum magnification";
        String text = field.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
    private void remember() { editors.forEach((field, editor) -> values.put(field, editor.getValue())); }
    @Override protected void init() {
        pageSize = Math.max(1, (height - 105) / 26);
        page = Math.min(page, (fields.size() - 1) / pageSize);
        editors.clear();
        int left = Math.max(8, width / 2 - 220), right = width / 2 + 5;
        for (int i = page * pageSize; i < Math.min(fields.size(), (page + 1) * pageSize); i++) {
            Field field = fields.get(i); int y = 42 + (i % pageSize) * 26;
            if (field.getType() == boolean.class || field.getType().isEnum()) {
                addRenderableWidget(Button.builder(Component.literal(values.get(field)), button -> {
                    String next;
                    if (field.getType() == boolean.class) next = Boolean.toString(!Boolean.parseBoolean(values.get(field)));
                    else {
                        Object[] options = field.getType().getEnumConstants(); int selected = 0;
                        for (int n = 0; n < options.length; n++) if (options[n].toString().equals(values.get(field))) selected = n;
                        next = options[(selected + 1) % options.length].toString();
                    }
                    values.put(field, next); button.setMessage(Component.literal(next));
                }).bounds(right, y, Math.min(210, width - right - 8), 20).build());
            } else {
                EditBox box = new EditBox(font, right, y, Math.min(210, width - right - 8), 20, Component.literal(label(field)));
                box.setMaxLength(2048); box.setValue(values.get(field)); editors.put(field, box); addRenderableWidget(box);
            }
        }
        addRenderableWidget(Button.builder(Component.literal("Previous"), b -> changePage(-1)).bounds(left,height-52,90,20).build()).active=page>0;
        addRenderableWidget(Button.builder(Component.literal("Next"), b -> changePage(1)).bounds(left+95,height-52,90,20).build()).active=(page+1)*pageSize<fields.size();
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> save()).bounds(right,height-52,90,20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose()).bounds(right+95,height-52,90,20).build());
    }
    private void changePage(int delta) { remember(); page += delta; rebuildWidgets(); }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void save() {
        remember(); ZoomConfig draft = new ZoomConfig();
        for (Field field : fields) {
            String text = values.get(field).trim(); Class<?> type = field.getType();
            try {
                Object value;
                if (type == boolean.class) value = Boolean.parseBoolean(text);
                else if (type == int.class) value = Integer.parseInt(text);
                else if (type == float.class) { float f = Float.parseFloat(text); if (!Float.isFinite(f)) throw new IllegalArgumentException(); if (field.getName().equals("minZoomMultiplier")) { if (f < 1 || f > 500) throw new IllegalArgumentException(); f = 1 / f; } value = f; }
                else if (type.isEnum()) value = Enum.valueOf((Class)type,text);
                else if (List.class.isAssignableFrom(type)) value = new ArrayList<>(Arrays.asList(text.split(",")));
                else value = text;
                field.set(draft, value);
            } catch (Exception e) { error = "Invalid value: " + label(field); return; }
        }
        draft.clamp();
        try { for (Field field : fields) field.set(ZoomConfig.INSTANCE,field.get(draft)); }
        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
        ZoomConfig.INSTANCE.save(); onClose();
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta) {
        graphics.fill(0,0,width,height,0xED101820);
        graphics.drawCenteredString(font,title,width/2,12,0xFFFFFFFF);
        graphics.drawCenteredString(font,"Page " + (page+1) + " / " + ((fields.size()+pageSize-1)/pageSize) + " · Profiles: comma-separated, in cycling order",width/2,27,0xFFBBBBBB);
        for (int i=page*pageSize;i<Math.min(fields.size(),(page+1)*pageSize);i++)
            graphics.drawString(font,label(fields.get(i)),Math.max(8,width/2-220),48+(i%pageSize)*26,0xFFFFFFFF);
        super.render(graphics,mouseX,mouseY,delta);
        graphics.drawCenteredString(font,error,width/2,height-23,0xFFFF8888);
    }
}
