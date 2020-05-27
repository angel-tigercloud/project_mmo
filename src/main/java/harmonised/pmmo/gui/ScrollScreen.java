package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ScrollScreen extends Screen
{
    private static double passiveMobHunterXp = Config.forgeConfig.passiveMobHunterXp.get();
    private static double aggresiveMobSlayerXp = Config.forgeConfig.aggresiveMobSlayerXp.get();
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );

    MainWindow sr = Minecraft.getInstance().getMainWindow();
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY, accumulativeHeight, buttonsSize, buttonsLoaded, futureHeight, minCount, maxCount;
    private MyScrollPanel scrollPanel;
    private final PlayerEntity player;
    private final String type;
    private ArrayList<ListButton> listButtons = new ArrayList<>();

    public ScrollScreen( ITextComponent titleIn, String type, PlayerEntity player )
    {
        super(titleIn);
        this.player = player;
        this.type = type;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        ArrayList<String> keyWords = new ArrayList<>();
        keyWords.add( "helmet" );
        keyWords.add( "chestplate" );
        keyWords.add( "leggings" );
        keyWords.add( "boots" );
        keyWords.add( "pickaxe" );
        keyWords.add( "axe" );
        keyWords.add( "shovel" );
        keyWords.add( "hoe" );
        keyWords.add( "sword" );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;

        Button exitButton = new TileButton(x + boxWidth - 24, y - 8, 0, 7, "", "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen(new SkillsScreen(getTransComp("pmmo.skills")));
        });

        Map<String, Map<String, Object>> reqMap = XP.getFullReqMap( type );

        ArrayList<ListButton> tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        switch( type )      //How it's made: Buttons!
        {
            case "biome":
            {
                Map<String, Map<String, Object>> bonusMap = JsonConfig.data.get( "biomeXpBonus" );
                Map<String, Map<String, Object>> scaleMap = JsonConfig.data.get( "biomeMobMultiplier" );
                List<String> biomesToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                if( bonusMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : bonusMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                if( scaleMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : scaleMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                biomesToAdd.sort( Comparator.comparingInt( b -> getReqCount( b, "biome" ) ) );

                for( String regKey : biomesToAdd )
                {
                    if ( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ) != null )
                    {
                        tempList.add( new ListButton( 0, 0, 1, 8, regKey, type, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case "dimension":
            {
                if( reqMap == null )
                    break;

                if( reqMap.containsKey( "all_dimensions" ) )
                {
                    tempList.add( new ListButton( 0, 0, 1, 8, "all_dimensions", type, "", button -> ((ListButton) button).clickAction() ) );
                }

                if( reqMap.containsKey( "minecraft:overworld" ) )
                {
                    tempList.add( new ListButton( 0, 0, 1, 8, "minecraft:overworld", type, "", button -> ((ListButton) button).clickAction() ) );
                }

                if( reqMap.containsKey( "minecraft:the_nether" ) )
                {
                    tempList.add( new ListButton( 0, 0, 1, 8, "minecraft:the_nether", type, "", button -> ((ListButton) button).clickAction() ) );
                }

                if( reqMap.containsKey( "minecraft:the_end" ) )
                {
                    tempList.add( new ListButton( 0, 0, 1, 8, "minecraft:the_end", type, "", button -> ((ListButton) button).clickAction() ) );
                }

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( !entry.getKey().equals( "all_dimensions" ) )
                    {
                        if ( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( entry.getKey() ) ) != null )
                        {
                            tempList.add( new ListButton( 0, 0, 1, 8, entry.getKey(), type, "", button -> ((ListButton) button).clickAction() ) );
                        }
                    }
                }
            }
                break;

            case "kill":
            {
                Map<String, Map<String, Object>> killXpMap = JsonConfig.data.get( "killXp" );
                Map<String, Map<String, Object>> rareDropMap = JsonConfig.data.get( "mobRareDrop" );
                ArrayList<String> mobsToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( killXpMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : killXpMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( rareDropMap != null )
                {
                    for( Map.Entry<String, Map<String, Object>> entry : rareDropMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                for( String regKey : mobsToAdd )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 1, 0, regKey, type, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case "breedXp":
            case "tameXp":
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 1, 0, entry.getKey(), type, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;

            case "fishEnchantPool":
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENCHANTMENTS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 0, 0, 1, 0, entry.getKey(), type, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
                break;

            default:
            {
                if( reqMap == null )
                    return;

                for( Map.Entry<String, Map<String, Object>> entry : reqMap.entrySet() )
                {
                    if( XP.getItem( entry.getKey() ) != Items.AIR )
                    {
                        tempList.add( new ListButton( 0, 0, 1, 0, entry.getKey(), type, "", button -> ((ListButton) button).clickAction() ) );
                    }
                }
            }
                break;
        }

        for( String keyWord : keyWords )
        {
            for( ListButton button : tempList)
            {
                if( button.regKey.contains( keyWord ) )
                {
                    if( !listButtons.contains(button) )
                        listButtons.add( button );
                }
            }
        }

        for( ListButton button : tempList)
        {
            if( !listButtons.contains( button ) )
                listButtons.add( button );
        }

        for( ListButton button : listButtons )
        {
            List<String> skillText = new ArrayList<>();
            List<String> scaleText = new ArrayList<>();
            List<String> effectText = new ArrayList<>();

            switch (type)   //Individual Button Handling
            {
                case "biome":
                {
                    if ( reqMap.containsKey( button.regKey ) )
                        addLevelsToButton(button, reqMap.get(button.regKey), player, false);

                    Map<String, Object> biomeBonusMap = JsonConfig.data.get("biomeXpBonus").get(button.regKey);
                    Map<String, Object> biomeMobMultiplierMap = JsonConfig.data.get("biomeMobMultiplier").get(button.regKey);
                    Map<String, Object> biomeEffectsMap = JsonConfig.data.get("biomeEffect").get(button.regKey);

                    if ( biomeBonusMap != null )
                    {
                        for (Map.Entry<String, Object> entry : biomeBonusMap.entrySet()) {
                            if ( (double) entry.getValue() > 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").setStyle(XP.skillStyle.get(Skill.getSkill(entry.getKey()))).getFormattedText());
                            if ( (double) entry.getValue() < 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").setStyle(XP.skillStyle.get(Skill.getSkill(entry.getKey()))).getFormattedText());
                        }
                    }

                    if ( biomeMobMultiplierMap != null )
                    {
                        for ( Map.Entry<String, Object> entry : biomeMobMultiplierMap.entrySet() )
                        {
                            Style styleColor = new Style();

                            if ( (double) entry.getValue() > 1 )
                                styleColor = XP.textStyle.get("red");
                            else if ( (double) entry.getValue() < 1 )
                                styleColor = XP.textStyle.get("green");

                            switch ( entry.getKey() )
                            {
                                case "damageBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleDamage", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;

                                case "hpBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleHp", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;

                                case "speedBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleSpeed", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;
                            }
                        }
                    }

                    if ( biomeEffectsMap != null )
                    {
                        for ( Map.Entry<String, Object> entry : biomeEffectsMap.entrySet() )
                        {
                            if ( ForgeRegistries.POTIONS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                            {
                                Effect effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );
                                if ( effect != null )
                                    effectText.add( " " + getTransComp( effect.getDisplayName().getFormattedText() + " " + (int) ( (double) entry.getValue() + 1) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                            }
                        }
                    }
                }
                    break;

                case "ore":
                case "log":
                case "plant":
                {
                    button.text.add( "" );
                    Map<String, Object> breakMap = JsonConfig.data.get( "breakReq" ).get( button.regKey );
                    Map<String, Double> infoMap = XP.getReqMap( button.regKey, type );
                    List<String> infoText = new ArrayList<>();
                    String transKey = "pmmo." + type + "ExtraDrop";
                    double extraDroppedPerLevel = infoMap.get( "extraChance" );
                    double extraDropped = XP.getExtraChance( player, button.regKey, type );

                    if ( extraDroppedPerLevel <= 0 )
                        infoText.add( getTransComp( "pmmo.extraDropPerLevel", DP.dp( extraDroppedPerLevel) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                    else
                        infoText.add( getTransComp( "pmmo.extraDropPerLevel", DP.dp( extraDroppedPerLevel) ).setStyle( XP.textStyle.get("green") ).getFormattedText() );

                    if ( extraDropped <= 0 )
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    else
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );

                    if ( infoText.size() > 0 )
                        button.text.addAll( infoText );

                    if ( breakMap != null )
                    {
                        if ( XP.checkReq( player, button.regKey, "break" ) )
                            button.text.add( getTransComp( "pmmo.break" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                        else
                            button.text.add( getTransComp( "pmmo.break" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        addLevelsToButton( button, breakMap, player, false );
                    }
                }
                    break;

                case "worn":
                {
                    button.text.add( "" );
                    addPercentageToButton( button, reqMap.get( button.regKey ), XP.checkReq( player, button.regKey, "wear" ) );
                }
                    break;

                case "held":
                {
                    button.text.add( "" );
                    addPercentageToButton( button, reqMap.get( button.regKey ), true );
                }
                    break;

                case "breedXp":
                case "tameXp":
                case "craftXp":
                case "breakXp":
                {
                    button.text.add( "" );
                    addXpToButton( button, reqMap.get( button.regKey ) );
                }
                    break;

                case "fishEnchantPool":
                {
                    Map<String, Object> enchantMap = reqMap.get( button.regKey );

                    double fishLevel = Skill.FISHING.getLevelDecimal( player );

                    double levelReq = (double) enchantMap.get( "levelReq" );
                    double chancePerLevel = (double) enchantMap.get( "chancePerLevel" );
                    double maxChance = (double) enchantMap.get( "maxChance" );
                    double maxLevel = (double) enchantMap.get( "maxLevel" );
                    double levelsPerTier = (double) enchantMap.get( "levelPerLevel" );

                    double curChance = (fishLevel - levelReq) * chancePerLevel;
                    if( curChance > maxChance )
                        curChance = maxChance;
                    if( curChance < 0 )
                        curChance = 0;

                    button.unlocked = levelReq <= fishLevel;
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    button.text.add( "" );

                    button.text.add( " " + getTransComp( "pmmo.currentChance", DP.dpSoft( curChance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getFormattedText() );

                    button.text.add( "" );
                    button.text.add( " " + getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).getFormattedText() );
                    if( maxLevel > 1 )
                        button.text.add( " " + getTransComp( "pmmo.levelsPerTier", DP.dpSoft( levelsPerTier ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.maxEnchantLevel", DP.dpSoft( maxLevel ) ).getFormattedText() );
                }
                    break;

                case "kill":
                {
                    Map<String, Object> killXpMap = JsonConfig.data.get("killXp").get( button.regKey );
                    Map<String, Object> rareDropMap = JsonConfig.data.get("mobRareDrop").get(button.regKey);
                    button.unlocked = XP.checkReq( player, button.regKey, type );
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    if ( reqMap.containsKey( button.regKey ) )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.toHarm" ).setStyle( color ).getFormattedText() );
                        addLevelsToButton( button, reqMap.get( button.regKey ), player, false );
                    }

                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.xpValue" ).setStyle( color ).getFormattedText() );
                    if ( killXpMap != null )
                        addXpToButton( button, killXpMap );
                    else
                    {
                        if( button.entity instanceof AnimalEntity )
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( passiveMobHunterXp ) ).setStyle( color ).getFormattedText() );
                        else if( button.entity instanceof MobEntity)
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( aggresiveMobSlayerXp ) ).setStyle( color ).getFormattedText() );
                    }

                    if ( rareDropMap != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.rareDrops" ).setStyle( color ).getFormattedText() );
                        for( Map.Entry<String, Object> entry : rareDropMap.entrySet() )
                        {
                            button.text.add( " " + new StringTextComponent( getTransComp( XP.getItem( entry.getKey() ).getTranslationKey() ).getFormattedText() + ": " + getTransComp( "pmmo.dropChance", DP.dpSoft( (double) entry.getValue() ) ).getFormattedText() ).setStyle( color ).getFormattedText() );
                        }
                    }
                }
                    break;

                case "dimension":
                {
                    if ( reqMap != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.veinBlacklist" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        for ( Map.Entry<String, Object> entry : reqMap.get( button.regKey ).entrySet() )
                        {
                            button.text.add( " " + getTransComp( XP.getItem( entry.getKey() ).getTranslationKey() ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        }
                    }
                }
                    break;

                case "fishPool":
                {
                    Map<String, Object> fishPoolMap = reqMap.get(button.regKey);

                    double level = Skill.FISHING.getLevelDecimal( player );
                    double weight = XP.getWeight( (int) level, fishPoolMap );
                    button.unlocked = weight > 0;
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    minCount = (int) (double) fishPoolMap.get( "minCount" );
                    maxCount = (int) (double) fishPoolMap.get( "maxCount" );

                    button.text.add( "" );
                    button.text.add( " " + getTransComp( "pmmo.currentWeight", weight ).setStyle( color ).getFormattedText() );

                    if ( minCount == maxCount )
                        button.text.add( " " + getTransComp( "pmmo.caughtAmount", minCount ).setStyle( color ).getFormattedText() );
                    else
                        button.text.add( " " + getTransComp( "pmmo.caughtAmountRange", minCount, maxCount ).setStyle( color ).getFormattedText() );

                    button.text.add( " " + getTransComp( "pmmo.xpEach", DP.dpSoft( (double) fishPoolMap.get("xp") ) ).setStyle( color ).getFormattedText() );

                    if ( button.itemStack.isEnchantable() )
                    {
                        if( (double) fishPoolMap.get( "enchantLevelReq" ) <= level && button.unlocked )
                            button.text.add( " " + getTransComp( "pmmo.enchantLevelReq", DP.dpSoft( (double) fishPoolMap.get( "enchantLevelReq" ) ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                        else
                            button.text.add( " " + getTransComp( "pmmo.enchantLevelReq", DP.dpSoft( (double) fishPoolMap.get( "enchantLevelReq" ) ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    }

                    button.text.add( "" );
//                    button.text.add( getTransComp( "pmmo.info" ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startWeight", DP.dpSoft( (double) fishPoolMap.get("startWeight") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startLevel", DP.dpSoft( (double) fishPoolMap.get("startLevel") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.endWeight", DP.dpSoft( (double) fishPoolMap.get("endWeight") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.endLevel", DP.dpSoft( (double) fishPoolMap.get("endLevel") ) ).getFormattedText() );
                }
                    break;

                case "wear":
                case "tool":
                case "weapon":
                case "use":
                case "break":
                case "place":
                {
                    button.text.add( "" );

                    if( XP.checkReq( player, button.regKey, type ) )
                        button.text.add( getTransComp( "pmmo." + type ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                    else
                        button.text.add( getTransComp( "pmmo." + type ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );

                    addLevelsToButton( button, reqMap.get( button.regKey ), player, false );
                }
                    break;

                case "salvagesFrom":
                {
                    Map<String, Map<String, Object>> salvagesToMap = XP.getFullReqMap( "salvagesTo" );
                    Map<String, Double> levelReqs = new HashMap<>();
                    List<String> sortedItems = new ArrayList<>();
                    if( reqMap == null || salvagesToMap == null )
                        return;
                    double smithLevel = Skill.SMITHING.getLevelDecimal( player );

                    boolean anyPassed = false;

                    for( Map.Entry<String, Object> entry : reqMap.get( button.regKey ).entrySet() )
                    {
                        double levelReq = (double) salvagesToMap.get( entry.getKey() ).get( "levelReq" );
                        levelReqs.put( entry.getKey(), levelReq );
                        sortedItems.add( entry.getKey() );
                    }

                    sortedItems.sort( Comparator.comparingDouble( a -> (double) reqMap.get( button.regKey ).get( a ) ) );
                    sortedItems.sort( Comparator.comparingDouble( levelReqs::get ) );

                    for( String key : sortedItems )
                    {
                        double levelReq = levelReqs.get( key );
                        boolean passed = levelReq <= smithLevel;
                        String itemName = getTransComp( XP.getItem( key ).getTranslationKey() ).getFormattedText();

                        double chance = (smithLevel - levelReq) * (double) salvagesToMap.get( key ).get( "chancePerLevel" );
                        double maxChance = (double) salvagesToMap.get( key ).get( "maxChance" );

                        if( chance > maxChance )
                            chance = maxChance;

                        if( passed )
                        {
                            anyPassed = true;
                            button.text.add( " " + getTransComp( "pmmo.valueValueChance", DP.dpSoft( (double) reqMap.get( button.regKey ).get( key ) ), itemName, chance ).setStyle( XP.textStyle.get( chance > 0 ? "green" : "red" ) ).getFormattedText() );
                        }
                        else
                            button.text.add( " " + getTransComp( "pmmo.salvagesFromLevelFromItem", DP.dpSoft( (double) reqMap.get( button.regKey ).get( key ) ), DP.dpSoft( levelReq ), itemName ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    }

                    button.unlocked = anyPassed;
                }
                    break;

                case "salvagesTo":
                {
                    if( reqMap == null )
                        return;
                    double smithLevel = Skill.SMITHING.getLevelDecimal( player );
                    String outputName = getTransComp( XP.getItem( (String) reqMap.get( button.regKey ).get( "salvageItem" ) ).getTranslationKey() ).getString();
                    double levelReq = (double) reqMap.get( button.regKey ).get( "levelReq" );
                    double salvageMax = (double) reqMap.get( button.regKey ).get( "salvageMax" );
                    double baseChance = (double) reqMap.get( button.regKey ).get( "baseChance" );
                    double chancePerLevel = (double) reqMap.get( button.regKey ).get( "chancePerLevel" );
                    double maxChance = (double) reqMap.get( button.regKey ).get( "maxChance" );
                    double xpPerItem = (double) reqMap.get( button.regKey ).get( "xpPerItem" );

                    double chance = 0;
                    button.unlocked = levelReq <= smithLevel;
                    if( button.unlocked )
                        chance = baseChance + ( smithLevel - levelReq ) * chancePerLevel;
                    if( chance > maxChance )
                        chance = maxChance;

                    Style color = XP.textStyle.get( chance > 0 ? "green" : "red" );

                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.canBeSalvagedFromLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.valueValue", DP.dpSoft( salvageMax ), outputName ).setStyle( color ).getFormattedText() );
                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.xpPerItem", DP.dpSoft( xpPerItem ) ).setStyle( color ).getFormattedText() );
                    button.text.add( getTransComp( "pmmo.chancePerItem", DP.dpSoft( chance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.baseChance", DP.dpSoft( baseChance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).setStyle( color ).getFormattedText() );
                    button.text.add( getTransComp( "pmmo.maxChancePerItem", DP.dpSoft( maxChance ) ).setStyle( color ).getFormattedText() );
                }
                    break;

                default:
                    break;
            }

            if( skillText.size() > 0 )
            {
                button.text.add( "" );
                skillText.sort( Comparator.comparingInt(ScrollScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.xpModifiers" ).getFormattedText() );
                button.text.addAll( skillText );
            }

            if( scaleText.size() > 0 )
            {
                if( skillText.size() > 0 )
                    button.text.add( "" );

                scaleText.sort( Comparator.comparingInt(ScrollScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.enemyScaling" ).getFormattedText() );
                button.text.addAll( scaleText );
            }

            if( effectText.size() > 0 )
            {
                if( skillText.size() > 0 || scaleText.size() > 0 )
                    button.text.add( "" );

                effectText.sort( Comparator.comparingInt(ScrollScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.biomeEffects" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                button.text.addAll( effectText );
            }

            switch( type )  //Unlock/Lock buttons if necessary
            {
                case "ore":
                case "log":
                case "plant":
                case "breakXp":
                        button.unlocked = XP.checkReq( player, button.regKey, "break" );
                    break;

                case "worn":
                        button.unlocked = XP.checkReq( player, button.regKey, "wear" );
                    break;

                case "fishPool":

                    break;

                case "wear":
                case "tool":
                case "weapon":
                case "use":
                case "break":
                case "place":
                case "biome":
                    button.unlocked = XP.checkReq( player, button.regKey, type );
                    break;

                default:
                    break;
            }
//            children.add( button );
        }

        //SORT BUTTONS

        switch( type )
        {
            case "ore":
            case "log":
            case "plant":
            case "breakXp":
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, "break" ) ) );
                break;

            case "worn":
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, "wear" ) ) );
                break;

            case "salvagesTo":
                listButtons.sort( Comparator.comparingDouble( b -> (double) reqMap.get( b.regKey ).get( "levelReq" ) ) );
                break;

            default:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, type ) ) );
                break;
        }
        scrollPanel = new MyScrollPanel( Minecraft.getInstance(), boxWidth - 42, boxHeight - 21, scrollY, scrollX, type, player, listButtons );

        children.add( scrollPanel );

        addButton(exitButton);
    }

    private static void addLevelsToButton( ListButton button, Map<String, Object> map, PlayerEntity player, boolean ignoreReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            if( !ignoreReq && Skill.getSkill( inEntry.getKey() ).getLevelDecimal( player ) < (double) inEntry.getValue() )
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ScrollScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Object> map )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ScrollScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addPercentageToButton( ListButton button, Map<String, Object> map, boolean metReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Object> inEntry : map.entrySet() )
        {
            double value = (double) inEntry.getValue();

            if( metReq )
            {
                if( value > 0 )
                    levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), "+" + value + "%" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                else if( value < 0 )
                    levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            }
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ScrollScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static int getTextInt( String comp )
    {
        String number = comp.replaceAll("\\D+","");

        if( number.length() > 0 && !Double.isNaN( Double.parseDouble( number ) ) )
            return (int) Double.parseDouble( number );
        else
            return 0;
    }

    private static int getReqCount( String regKey, String type )
    {
        Map<String, Double> map = XP.getReqMap( regKey, type );

        if( map == null )
            return 0;
        else
            return map.size();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        scrollPanel.render( mouseX, mouseY, partialTicks );

        ListButton button;

        accumulativeHeight = 0;
        buttonsSize = listButtons.size();

        for( int i = 0; i < buttonsSize; i++ )
        {
            button = listButtons.get( i );

            buttonX = mouseX - button.x;
            buttonY = mouseY - button.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                if( type.equals( "biome" ) || type.equals( "kill" ) || type.equals( "breedXp" ) || type.equals( "tameXp" ) || type.equals( "dimension" ) || type.equals( "fishEnchantPool" ) )
                    renderTooltip( button.title, mouseX, mouseY );
                else if( button.itemStack != null )
                    renderTooltip( button.itemStack, mouseX, mouseY );
            }

            accumulativeHeight += button.getHeight();
        }

//        renderTooltip( mouseX + " " + mouseY, mouseX, mouseY );
//        drawCenteredString(Minecraft.getInstance().fontRenderer, player.getDisplayName().getString() + " " + type,x + boxWidth / 2, y + boxHeight / 2, 50000 );
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        for( ListButton a : listButtons )
        {
            int buttonX = (int) mouseX - a.x;
            int buttonY = (int) mouseY- a.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                a.onClick( mouseX, mouseY );
            }

        }
        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    public static TranslationTextComponent getTransComp( String translationKey, Object... args )
    {
        return new TranslationTextComponent( translationKey, args );
    }

}