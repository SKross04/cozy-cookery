package net.skross.cozycookery.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.ModBlocks;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.recipe.BakingRecipe;
import net.skross.cozycookery.recipe.ChoppingRecipe;
import net.skross.cozycookery.recipe.CookingRecipe;
import net.skross.cozycookery.recipe.FryingRecipe;
import net.skross.cozycookery.recipe.GrindingRecipe;
import net.skross.cozycookery.recipe.MixingRecipe;
import net.skross.cozycookery.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Cozy Cookbook: an illustrated two-page book.
 * Title, index, a "how to" page per station, then one chapter per station with every recipe,
 * read live from the recipe files (new recipes show up automatically).
 */
public class CookbookScreen extends Screen {
    private static final ResourceLocation BOOK =
            ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "textures/gui/cookbook.png");
    private static final int BOOK_W = 256;
    private static final int BOOK_H = 180;
    private static final int PAGE_W = 104;           // usable width of one page
    private static final int LEFT_PAGE_X = 14;
    private static final int RIGHT_PAGE_X = 138;
    private static final int PAGE_Y = 12;
    private static final int INK = 0xFF3A2414;
    private static final int SOFT_INK = 0xFF7A5A3A;
    private static final int RED_INK = 0xFFA8301E;

    // ---------- Book content ----------
    private record Entry(ItemStack result, List<Ingredient> ingredients, @Nullable Ingredient tool, Component note) {
    }

    private interface Page {
        void render(GuiGraphics g, int x, int y, int mouseX, int mouseY);
    }

    private record Link(int x, int y, int w, int h, int page) {
    }

    private final List<Page> pages = new ArrayList<>();
    private final List<Component> chapterTitles = new ArrayList<>();
    private final List<Integer> chapterPages = new ArrayList<>();
    private final List<Link> links = new ArrayList<>(); // clickable spots on the current spread
    private int spread = 0;                              // left page = spread * 2
    private int left;
    private int top;
    private ItemStack hovered = ItemStack.EMPTY;
    private PageButton next;
    private PageButton previous;

    public CookbookScreen() {
        super(Component.translatable("item.cozycookery.cookbook"));
    }

    @Override
    protected void init() {
        left = (this.width - BOOK_W) / 2;
        top = (this.height - BOOK_H) / 2;
        buildPages();
        next = this.addRenderableWidget(new PageButton(left + BOOK_W - 44, top + BOOK_H - 4, true,
                b -> turnTo(spread + 1), true));
        previous = this.addRenderableWidget(new PageButton(left + 20, top + BOOK_H - 4, false,
                b -> turnTo(spread - 1), true));
        updateButtons();
    }

    private void turnTo(int newSpread) {
        int last = (pages.size() - 1) / 2;
        spread = Math.max(0, Math.min(last, newSpread));
        updateButtons();
    }

    private void updateButtons() {
        next.visible = spread < (pages.size() - 1) / 2;
        previous.visible = spread > 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------- Building the pages ----------
    private void buildPages() {
        pages.clear();
        chapterTitles.clear();
        chapterPages.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        RecipeManager rm = mc.level.getRecipeManager();
        HolderLookup.Provider reg = mc.level.registryAccess();

        pages.add(this::titlePage);
        pages.add(this::indexPage);

        // How each station works
        chapter(Component.translatable("cookbook.cozycookery.chapter.stations"));
        guide("chopping_board", ModBlocks.CHOPPING_BOARDS.values().iterator().next().get());
        guide("stove", ModBlocks.STOVES.values().iterator().next().get());
        guide("cooking_pot", ModBlocks.COOKING_POT.get());
        guide("frying_pan", ModBlocks.FRYING_PAN.get());
        guide("mortar", ModBlocks.MORTAR.get());
        guide("mixing_bowl", ModBlocks.MIXING_BOWL.get());
        guide("oven", ModBlocks.OVENS.values().iterator().next().get());
        guide("plates", ModItems.PLATE.get());
        guide("farming", ModItems.TOMATO_SEEDS.get());

        // One chapter per station
        List<Entry> chopping = new ArrayList<>();
        for (RecipeHolder<ChoppingRecipe> h : rm.getAllRecipesFor(ModRecipes.CHOPPING_TYPE.get())) {
            ChoppingRecipe r = h.value();
            ItemStack out = r.results().isEmpty() ? ItemStack.EMPTY : r.results().get(0);
            chopping.add(new Entry(out, List.of(r.ingredient()), r.tool(),
                    Component.translatable("cookbook.cozycookery.note.chopping", r.results().size() > 1
                            ? Component.translatable("cookbook.cozycookery.note.extra_results", r.results().size() - 1)
                            : Component.empty())));
        }
        recipeChapter("chopping", chopping);

        List<Entry> cooking = new ArrayList<>();
        for (RecipeHolder<CookingRecipe> h : rm.getAllRecipesFor(ModRecipes.COOKING_TYPE.get())) {
            CookingRecipe r = h.value();
            cooking.add(new Entry(r.getResultItem(reg), r.ingredients(), null,
                    Component.translatable(r.needsBowl() ? "cookbook.cozycookery.note.cooking_bowl"
                            : "cookbook.cozycookery.note.cooking_hand", r.cookingTime() / 40)));
        }
        recipeChapter("cooking", cooking);

        List<Entry> frying = new ArrayList<>();
        for (RecipeHolder<FryingRecipe> h : rm.getAllRecipesFor(ModRecipes.FRYING_TYPE.get())) {
            FryingRecipe r = h.value();
            frying.add(new Entry(r.getResultItem(reg), List.of(r.ingredient()), null,
                    Component.translatable("cookbook.cozycookery.note.frying", r.cookingTime() / 20)));
        }
        recipeChapter("frying", frying);

        List<Entry> grinding = new ArrayList<>();
        for (RecipeHolder<GrindingRecipe> h : rm.getAllRecipesFor(ModRecipes.GRINDING_TYPE.get())) {
            GrindingRecipe r = h.value();
            grinding.add(new Entry(r.getResultItem(reg), List.of(r.ingredient()), null,
                    Component.translatable("cookbook.cozycookery.note.grinding")));
        }
        recipeChapter("grinding", grinding);

        List<Entry> mixing = new ArrayList<>();
        for (RecipeHolder<MixingRecipe> h : rm.getAllRecipesFor(ModRecipes.MIXING_TYPE.get())) {
            MixingRecipe r = h.value();
            mixing.add(new Entry(r.getResultItem(reg), r.ingredients(), null,
                    Component.translatable("cookbook.cozycookery.note.mixing")));
        }
        recipeChapter("mixing", mixing);

        List<Entry> baking = new ArrayList<>();
        for (RecipeHolder<BakingRecipe> h : rm.getAllRecipesFor(ModRecipes.BAKING_TYPE.get())) {
            BakingRecipe r = h.value();
            baking.add(new Entry(r.getResultItem(reg), List.of(r.ingredient()), null,
                    Component.translatable("cookbook.cozycookery.note.baking", r.cookingTime() / 20)));
        }
        recipeChapter("baking", baking);

        // Plated meals are crafting recipes in data/cozycookery/recipe/plating/
        List<Entry> plating = new ArrayList<>();
        for (var h : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
            ResourceLocation id = h.id();
            if (id.getNamespace().equals(CozyCookery.MOD_ID) && id.getPath().startsWith("plating/")) {
                plating.add(new Entry(h.value().getResultItem(reg), h.value().getIngredients(), null,
                        Component.translatable("cookbook.cozycookery.note.plating")));
            }
        }
        recipeChapter("plating", plating);

        if (pages.size() % 2 == 1) {
            pages.add((g, x, y, mx, my) -> {
            });
        }
    }

    private void chapter(Component title) {
        chapterTitles.add(title);
        chapterPages.add(pages.size());
    }

    private void guide(String key, ItemLike icon) {
        ItemStack stack = new ItemStack(icon);
        Component title = Component.translatable("cookbook.cozycookery.guide." + key);
        Component text = Component.translatable("cookbook.cozycookery.guide." + key + ".text");
        pages.add((g, x, y, mx, my) -> {
            item(g, stack, x, y, mx, my);
            g.drawString(this.font, title, x + 20, y + 4, RED_INK, false);
            int ty = y + 22;
            for (FormattedCharSequence line : this.font.split(text, PAGE_W)) {
                g.drawString(this.font, line, x, ty, INK, false);
                ty += 10;
            }
        });
    }

    // Recipes are packed onto pages by their height; the chapter title sits on the first page
    private static final int PAGE_HEIGHT = 146;
    private static final int PER_ROW = 6;
    private static final int CELL = 17;

    private int entryHeight(Entry e) {
        int count = e.ingredients().size() + (e.tool() != null ? 1 : 0);
        int rows = (count + PER_ROW - 1) / PER_ROW;
        int noteLines = Math.max(1, this.font.split(e.note(), (int) (PAGE_W / 0.8f)).size());
        return 20 + rows * CELL + 3 + noteLines * 8 + 6;
    }

    private void recipeChapter(String key, List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        entries.sort(Comparator.comparing(e -> e.result().getHoverName().getString()));
        // chapters start on a left page, so a chapter always opens as a fresh spread
        if (pages.size() % 2 == 1) {
            pages.add((g, x, y, mx, my) -> {
            });
        }
        Component title = Component.translatable("cookbook.cozycookery.chapter." + key);
        chapter(title);
        int i = 0;
        boolean first = true;
        while (i < entries.size()) {
            int used = first ? 16 : 0;
            List<Entry> onPage = new ArrayList<>();
            while (i < entries.size() && (onPage.isEmpty() || used + entryHeight(entries.get(i)) <= PAGE_HEIGHT)) {
                used += entryHeight(entries.get(i));
                onPage.add(entries.get(i));
                i++;
            }
            boolean withTitle = first;
            pages.add((g, x, y, mx, my) -> {
                int ey = y;
                if (withTitle) {
                    g.drawString(this.font, title, x + (PAGE_W - this.font.width(title)) / 2, y, RED_INK, false);
                    g.fill(x + 10, y + 11, x + PAGE_W - 10, y + 12, SOFT_INK);
                    ey += 16;
                }
                for (Entry e : onPage) {
                    drawEntry(g, e, x, ey, mx, my);
                    ey += entryHeight(e);
                }
            });
            first = false;
        }
    }

    // ---------- Drawing ----------
    private void titlePage(GuiGraphics g, int x, int y, int mx, int my) {
        Component t1 = Component.translatable("cookbook.cozycookery.title");
        Component t2 = Component.translatable("cookbook.cozycookery.subtitle");
        g.pose().pushPose();
        g.pose().translate(x + PAGE_W / 2f, y + 30, 0);
        g.pose().scale(1.5f, 1.5f, 1f);
        g.drawString(this.font, t1, -this.font.width(t1) / 2, 0, RED_INK, false);
        g.pose().popPose();
        g.drawString(this.font, t2, x + (PAGE_W - this.font.width(t2)) / 2, y + 48, SOFT_INK, false);
        // a little still life: tomato, knife, bowl of stew
        g.pose().pushPose();
        g.pose().translate(x + PAGE_W / 2f - 36, y + 72, 0);
        g.pose().scale(1.5f, 1.5f, 1f);
        g.renderItem(new ItemStack(ModItems.TOMATO.get()), 0, 0);
        g.renderItem(new ItemStack(ModItems.IRON_KNIFE.get()), 16, 0);
        g.renderItem(new ItemStack(ModItems.BEEF_STEW.get()), 32, 0);
        g.pose().popPose();
        Component hint = Component.translatable("cookbook.cozycookery.title_hint");
        int ty = y + 110;
        for (FormattedCharSequence line : this.font.split(hint, PAGE_W)) {
            g.drawString(this.font, line, x + (PAGE_W - this.font.width(line)) / 2, ty, SOFT_INK, false);
            ty += 10;
        }
    }

    private void indexPage(GuiGraphics g, int x, int y, int mx, int my) {
        Component t = Component.translatable("cookbook.cozycookery.index");
        g.drawString(this.font, t, x + (PAGE_W - this.font.width(t)) / 2, y, RED_INK, false);
        g.fill(x + 10, y + 11, x + PAGE_W - 10, y + 12, SOFT_INK);
        for (int i = 0; i < chapterTitles.size(); i++) {
            int ly = y + 20 + i * 14;
            Component c = chapterTitles.get(i);
            boolean over = mx >= x && mx < x + PAGE_W && my >= ly - 2 && my < ly + 10;
            g.drawString(this.font, Component.literal("• ").append(c), x + 4, ly, over ? RED_INK : INK, false);
            String num = String.valueOf(chapterPages.get(i) + 1);
            g.drawString(this.font, num, x + PAGE_W - this.font.width(num), ly, SOFT_INK, false);
            links.add(new Link(x, ly - 2, PAGE_W, 12, chapterPages.get(i)));
        }
    }

    private void drawEntry(GuiGraphics g, Entry e, int x, int y, int mx, int my) {
        // result: icon + name
        item(g, e.result(), x, y, mx, my);
        Component name = e.result().getHoverName();
        List<FormattedCharSequence> lines = this.font.split(name, PAGE_W - 20);
        g.drawString(this.font, lines.get(0), x + 20, y + (lines.size() > 1 ? 0 : 4), INK, false);
        if (lines.size() > 1) {
            g.drawString(this.font, lines.get(1), x + 20, y + 9, INK, false);
        }
        // ingredients (+ the tool, on a reddish square), 6 per row
        int iy = y + 20;
        List<Ingredient> all = new ArrayList<>(e.ingredients());
        if (e.tool() != null) {
            all.add(e.tool());
        }
        for (int i = 0; i < all.size(); i++) {
            int ix = x + (i % PER_ROW) * CELL;
            int row = iy + (i / PER_ROW) * CELL;
            boolean isTool = e.tool() != null && i == all.size() - 1;
            g.fill(ix, row, ix + 16, row + 16, isTool ? 0x40A8301E : 0x28000000);
            item(g, cycle(all.get(i)), ix, row, mx, my);
        }
        int rows = (all.size() + PER_ROW - 1) / PER_ROW;
        int ny = iy + rows * CELL + 3;
        for (FormattedCharSequence line : this.font.split(e.note(), (int) (PAGE_W / 0.8f))) {
            g.pose().pushPose();
            g.pose().translate(x, ny, 0);
            g.pose().scale(0.8f, 0.8f, 1f);
            g.drawString(this.font, line, 0, 0, SOFT_INK, false);
            g.pose().popPose();
            ny += 8;
        }
    }

    // An ingredient that accepts several items shows them one after another
    private static ItemStack cycle(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) {
            return new ItemStack(Items.BARRIER);
        }
        long t = System.currentTimeMillis() / 1000;
        return items[(int) (t % items.length)];
    }

    private void item(GuiGraphics g, ItemStack stack, int x, int y, int mx, int my) {
        if (stack.isEmpty()) {
            return;
        }
        g.renderItem(stack, x, y);
        g.renderItemDecorations(this.font, stack, x, y);
        if (mx >= x && mx < x + 16 && my >= y && my < y + 16) {
            hovered = stack;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.blit(BOOK, left, top, 0, 0, BOOK_W, BOOK_H, 256, 256);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick); // background, book and page buttons
        hovered = ItemStack.EMPTY;
        links.clear();
        int li = spread * 2;
        if (li < pages.size()) {
            pages.get(li).render(g, left + LEFT_PAGE_X, top + PAGE_Y, mouseX, mouseY);
            pageNumber(g, li, left + LEFT_PAGE_X);
        }
        if (li + 1 < pages.size()) {
            pages.get(li + 1).render(g, left + RIGHT_PAGE_X, top + PAGE_Y, mouseX, mouseY);
            pageNumber(g, li + 1, left + RIGHT_PAGE_X);
        }
        // back to the index from anywhere
        if (spread > 0) {
            Component back = Component.translatable("cookbook.cozycookery.back_to_index");
            int bx = left + (BOOK_W - this.font.width(back)) / 2;
            int by = top + BOOK_H + 2;
            boolean over = mouseX >= bx && mouseX < bx + this.font.width(back) && mouseY >= by && mouseY < by + 9;
            g.drawString(this.font, back, bx, by, over ? 0xFFFFE080 : 0xFFFFFFFF, true);
            links.add(new Link(bx, by, this.font.width(back), 9, 1));
        }
        if (!hovered.isEmpty()) {
            g.renderTooltip(this.font, hovered, mouseX, mouseY);
        }
    }

    private void pageNumber(GuiGraphics g, int index, int x) {
        String n = String.valueOf(index + 1);
        g.drawString(this.font, n, x + (PAGE_W - this.font.width(n)) / 2, top + BOOK_H - 18, SOFT_INK, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Link link : links) {
            if (mouseX >= link.x() && mouseX < link.x() + link.w() && mouseY >= link.y() && mouseY < link.y() + link.h()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
                turnTo(link.page() / 2);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            turnTo(spread + (scrollY < 0 ? 1 : -1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
