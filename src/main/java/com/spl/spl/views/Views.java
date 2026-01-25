package com.spl.spl.views;

public class Views {
    

	// Base view - minimal fields
    public static class Base {}
    
    // Summary view - extends Base
    public static class Summary extends Base {}
    
    // Detailed view - extends Summary  
    public static class Detailed extends Summary {}
    
    // Domain-specific views
    public static class TeamSeasonsView extends Summary {}
    public static class TeamSeasonView extends Summary {}
    public static class PlayerTeamView extends Detailed {}
    public static class AdminView extends Detailed {}
    public static class SeasonView extends Summary {}
    public static class PlayerLevel extends Base {}
}