package com.moodtracking.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moodtracking.dto.MovieRecommendation;
import com.moodtracking.dto.SongRecommendation;
import com.moodtracking.models.Mood.MoodType;
import com.moodtracking.models.User;

@Service
public class RecommendationService {
    
    @Autowired
    private MoodService moodService;
    
    private final Map<MoodType, List<SongRecommendation>> moodToSongs;
    private final Map<MoodType, List<MovieRecommendation>> moodToMovies;
    
    public RecommendationService() {
        this.moodToSongs = initializeSongRecommendations();
        this.moodToMovies = initializeMovieRecommendations();
    }
    
    public List<SongRecommendation> getSongRecommendations(MoodType moodType) {
        return moodToSongs.getOrDefault(moodType, new ArrayList<>());
    }
    
    public List<MovieRecommendation> getMovieRecommendations(MoodType moodType) {
        return moodToMovies.getOrDefault(moodType, new ArrayList<>());
    }
    
    public List<SongRecommendation> getSongRecommendationsForUser(User user) {
        MoodType recentMood = getMostRecentMoodType(user);
        if (recentMood != null) {
            return getSongRecommendations(recentMood);
        }
        return getDefaultSongRecommendations();
    }
    
    public List<MovieRecommendation> getMovieRecommendationsForUser(User user) {
        MoodType recentMood = getMostRecentMoodType(user);
        if (recentMood != null) {
            return getMovieRecommendations(recentMood);
        }
        return getDefaultMovieRecommendations();
    }
    
    public MoodType getMostRecentMoodType(User user) {
        try {
            var recentMoods = moodService.getRecentMoods(user);
            if (!recentMoods.isEmpty()) {
                return recentMoods.get(0).getMoodType();
            }
        } catch (Exception e) {
            // Handle error gracefully
        }
        return null;
    }
    
    private Map<MoodType, List<SongRecommendation>> initializeSongRecommendations() {
        Map<MoodType, List<SongRecommendation>> songs = new HashMap<>();
        
        // HAPPY songs
        songs.put(MoodType.HAPPY, Arrays.asList(
            new SongRecommendation("Happy", "Pharrell Williams", "G I R L", "Pop"),
            new SongRecommendation("Good as Hell", "Lizzo", "Cuz I Love You", "Pop"),
            new SongRecommendation("Can't Stop the Feeling!", "Justin Timberlake", "Trolls Soundtrack", "Pop"),
            new SongRecommendation("Walking on Sunshine", "Katrina and the Waves", "Walking on Sunshine", "Rock"),
            new SongRecommendation("I Got You (I Feel Good)", "James Brown", "I Got You", "Soul")
        ));
        
        // SAD songs
        songs.put(MoodType.SAD, Arrays.asList(
            new SongRecommendation("Someone Like You", "Adele", "21", "Pop"),
            new SongRecommendation("Mad World", "Gary Jules", "Donnie Darko Soundtrack", "Alternative"),
            new SongRecommendation("Black", "Pearl Jam", "Ten", "Grunge"),
            new SongRecommendation("Hurt", "Johnny Cash", "American IV: The Man Comes Around", "Country"),
            new SongRecommendation("Everybody Hurts", "R.E.M.", "Automatic for the People", "Alternative Rock")
        ));
        
        // STRESSED songs
        songs.put(MoodType.STRESSED, Arrays.asList(
            new SongRecommendation("Weightless", "Marconi Union", "Weightless", "Ambient"),
            new SongRecommendation("Clair de Lune", "Claude Debussy", "Suite Bergamasque", "Classical"),
            new SongRecommendation("Aqueous Transmission", "Incubus", "Morning View", "Alternative Rock"),
            new SongRecommendation("River", "Joni Mitchell", "Blue", "Folk"),
            new SongRecommendation("Spiegel im Spiegel", "Arvo Pärt", "Tabula Rasa", "Contemporary Classical")
        ));
        
        // RELAXED songs
        songs.put(MoodType.RELAXED, Arrays.asList(
            new SongRecommendation("Fly Me to the Moon", "Frank Sinatra", "It Might as Well Be Swing", "Jazz"),
            new SongRecommendation("The Girl from Ipanema", "Stan Getz & Astrud Gilberto", "Getz/Gilberto", "Bossa Nova"),
            new SongRecommendation("Blue in Green", "Miles Davis", "Kind of Blue", "Jazz"),
            new SongRecommendation("Teardrop", "Massive Attack", "Mezzanine", "Trip Hop"),
            new SongRecommendation("Mad About You", "Sting", "Ten Summoner's Tales", "Pop")
        ));
        
        // ANXIOUS songs
        songs.put(MoodType.ANXIOUS, Arrays.asList(
            new SongRecommendation("Breathe Me", "Sia", "1000 Forms of Fear", "Pop"),
            new SongRecommendation("Anxiety", "Julia Michaels ft. Selena Gomez", "Inner Monologue Part 1", "Pop"),
            new SongRecommendation("Heavy", "Linkin Park ft. Kiiara", "One More Light", "Alternative Rock"),
            new SongRecommendation("Unwell", "Matchbox Twenty", "More Than You Think You Are", "Rock"),
            new SongRecommendation("The Sound of Silence", "Simon & Garfunkel", "Sounds of Silence", "Folk Rock")
        ));
        
        // EXCITED songs
        songs.put(MoodType.EXCITED, Arrays.asList(
            new SongRecommendation("Uptown Funk", "Mark Ronson ft. Bruno Mars", "Uptown Special", "Funk"),
            new SongRecommendation("I Want It That Way", "Backstreet Boys", "Millennium", "Pop"),
            new SongRecommendation("Don't Stop Me Now", "Queen", "Jazz", "Rock"),
            new SongRecommendation("September", "Earth, Wind & Fire", "The Best of Earth, Wind & Fire", "Funk"),
            new SongRecommendation("Good Time", "Owl City & Carly Rae Jepsen", "The Midsummer Station", "Pop")
        ));
        
        // ANGRY songs
        songs.put(MoodType.ANGRY, Arrays.asList(
            new SongRecommendation("Break Stuff", "Limp Bizkit", "Significant Other", "Nu Metal"),
            new SongRecommendation("Bodies", "Drowning Pool", "Sinner", "Metal"),
            new SongRecommendation("Killing in the Name", "Rage Against the Machine", "Rage Against the Machine", "Rap Metal"),
            new SongRecommendation("Chop Suey!", "System of a Down", "Toxicity", "Alternative Metal"),
            new SongRecommendation("In the End", "Linkin Park", "Hybrid Theory", "Nu Metal")
        ));
        
        // CONTENT songs
        songs.put(MoodType.CONTENT, Arrays.asList(
            new SongRecommendation("Three Little Birds", "Bob Marley", "Exodus", "Reggae"),
            new SongRecommendation("What a Wonderful World", "Louis Armstrong", "What a Wonderful World", "Jazz"),
            new SongRecommendation("Here Comes the Sun", "The Beatles", "Abbey Road", "Rock"),
            new SongRecommendation("Perfect", "Ed Sheeran", "÷ (Divide)", "Pop"),
            new SongRecommendation("Feeling Good", "Nina Simone", "I Put a Spell on You", "Jazz")
        ));
        
        // TIRED songs
        songs.put(MoodType.TIRED, Arrays.asList(
            new SongRecommendation("Tired", "Alan Walker ft. Gavin James", "World of Walker", "Electronic"),
            new SongRecommendation("I'm So Tired...", "Lauv & Troye Sivan", "~how i'm feeling~", "Pop"),
            new SongRecommendation("Sleepyhead", "Passion Pit", "Manners", "Indie Pop"),
            new SongRecommendation("Mr. Sandman", "The Chordettes", "Mr. Sandman", "Pop"),
            new SongRecommendation("Weightless", "Marconi Union", "Weightless", "Ambient")
        ));
        
        // ENERGETIC songs
        songs.put(MoodType.ENERGETIC, Arrays.asList(
            new SongRecommendation("Energy", "Calvin Harris", "Motion", "Electronic Dance"),
            new SongRecommendation("Pump It", "Black Eyed Peas", "Monkey Business", "Hip Hop"),
            new SongRecommendation("Thunder", "Imagine Dragons", "Evolve", "Pop Rock"),
            new SongRecommendation("Can't Hold Us", "Macklemore & Ryan Lewis", "The Heist", "Hip Hop"),
            new SongRecommendation("High Hopes", "Panic! At The Disco", "Pray for the Wicked", "Pop Rock")
        ));
        
        return songs;
    }
    
    private Map<MoodType, List<MovieRecommendation>> initializeMovieRecommendations() {
        Map<MoodType, List<MovieRecommendation>> movies = new HashMap<>();
        
        // HAPPY movies
        movies.put(MoodType.HAPPY, Arrays.asList(
            new MovieRecommendation("The Pursuit of Happyness", "Gabriele Muccino", 2006, "Drama", 
                "A struggling salesman takes custody of his son as he's poised to begin a life-changing professional career."),
            new MovieRecommendation("Paddington", "Paul King", 2014, "Comedy/Family", 
                "A young Peruvian bear travels to London in search of a home."),
            new MovieRecommendation("The Grand Budapest Hotel", "Wes Anderson", 2014, "Comedy/Drama", 
                "A writer encounters the owner of an aging high-class hotel."),
            new MovieRecommendation("Mamma Mia!", "Phyllida Lloyd", 2008, "Musical/Comedy", 
                "The story of a bride-to-be trying to find her real father told using hit songs by ABBA."),
            new MovieRecommendation("Inside Out", "Pete Docter", 2015, "Animation/Family", 
                "After young Riley is uprooted from her Midwest life, her emotions conflict on how best to navigate a new city.")
        ));
        
        // SAD movies
        movies.put(MoodType.SAD, Arrays.asList(
            new MovieRecommendation("The Fault in Our Stars", "Josh Boone", 2014, "Drama/Romance", 
                "Two teenage cancer patients begin a life-affirming journey to visit a reclusive author in Amsterdam."),
            new MovieRecommendation("Marley & Me", "David Frankel", 2008, "Comedy/Drama", 
                "A family learns important life lessons from their adorable, but naughty and neurotic dog."),
            new MovieRecommendation("A Star Is Born", "Bradley Cooper", 2018, "Drama/Romance", 
                "A musician helps a young singer find fame as age and alcoholism send his own career into a downward spiral."),
            new MovieRecommendation("Manchester by the Sea", "Kenneth Lonergan", 2016, "Drama", 
                "A depressed uncle is asked to take care of his teenage nephew after the boy's father dies."),
            new MovieRecommendation("Her", "Spike Jonze", 2013, "Drama/Romance", 
                "A writer develops a relationship with an operating system designed to meet his every need.")
        ));
        
        // STRESSED movies
        movies.put(MoodType.STRESSED, Arrays.asList(
            new MovieRecommendation("My Neighbor Totoro", "Hayao Miyazaki", 1988, "Animation/Family", 
                "When two girls move to the country to be near their ailing mother, they have adventures with forest spirits."),
            new MovieRecommendation("Spirited Away", "Hayao Miyazaki", 2001, "Animation/Family", 
                "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods and witches."),
            new MovieRecommendation("Finding Nemo", "Andrew Stanton", 2003, "Animation/Family", 
                "After his son is captured, a timid clownfish sets out on a journey to find him."),
            new MovieRecommendation("The Secret Garden", "Agnieszka Holland", 1993, "Drama/Family", 
                "A young, recently-orphaned girl is sent to England after living in India all of her life."),
            new MovieRecommendation("Kiki's Delivery Service", "Hayao Miyazaki", 1989, "Animation/Family", 
                "A young witch moves to a new town and uses her flying ability to earn a living.")
        ));
        
        // RELAXED movies
        movies.put(MoodType.RELAXED, Arrays.asList(
            new MovieRecommendation("Lost in Translation", "Sofia Coppola", 2003, "Drama/Comedy", 
                "A faded movie star and a neglected young woman form an unlikely bond after crossing paths in Tokyo."),
            new MovieRecommendation("Before Sunset", "Richard Linklater", 2004, "Drama/Romance", 
                "Nine years after Jesse and Celine first met, they encounter each other again on the French leg of Jesse's book tour."),
            new MovieRecommendation("Julie & Julia", "Nora Ephron", 2009, "Drama/Comedy", 
                "Julia Child's story of her start in the cooking profession is intertwined with blogger Julie Powell's 2002 challenge."),
            new MovieRecommendation("Midnight in Paris", "Woody Allen", 2011, "Comedy/Fantasy", 
                "While on a trip to Paris with his fiancée's family, a nostalgic screenwriter finds himself transported back to the 1920s."),
            new MovieRecommendation("The Princess Bride", "Rob Reiner", 1987, "Adventure/Comedy", 
                "A bedridden boy's grandfather reads him the story of a farmboy-turned-pirate who encounters numerous obstacles.")
        ));
        
        // ANXIOUS movies
        movies.put(MoodType.ANXIOUS, Arrays.asList(
            new MovieRecommendation("Inside Out", "Pete Docter", 2015, "Animation/Family", 
                "After young Riley is uprooted, her emotions conflict on how best to navigate a new city."),
            new MovieRecommendation("A Beautiful Mind", "Ron Howard", 2001, "Drama/Biography", 
                "After John Nash suffers a mental breakdown, he learns to live with his condition with the help of his wife."),
            new MovieRecommendation("Silver Linings Playbook", "David O. Russell", 2012, "Drama/Comedy", 
                "After a stint in a mental institution, Pat moves back in with his parents and tries to win back his ex-wife."),
            new MovieRecommendation("Good Will Hunting", "Gus Van Sant", 1997, "Drama", 
                "Will Hunting has a genius-level IQ but chooses to work as a janitor at MIT."),
            new MovieRecommendation("The Perks of Being a Wallflower", "Stephen Chbosky", 2012, "Drama", 
                "An introvert freshman is taken under the wings of two charismatic seniors who welcome him to the real world.")
        ));
        
        // EXCITED movies
        movies.put(MoodType.EXCITED, Arrays.asList(
            new MovieRecommendation("The Avengers", "Joss Whedon", 2012, "Action/Adventure", 
                "Earth's mightiest heroes must come together and learn to fight as a team."),
            new MovieRecommendation("Guardians of the Galaxy", "James Gunn", 2014, "Action/Adventure", 
                "A group of intergalactic criminals must pull together to stop a fanatical warrior."),
            new MovieRecommendation("Spider-Man: Into the Spider-Verse", "Bob Persichetti", 2018, "Animation/Action", 
                "Teen Miles Morales becomes Spider-Man of his reality, crossing his path with five counterparts."),
            new MovieRecommendation("Mad Max: Fury Road", "George Miller", 2015, "Action/Adventure", 
                "In a post-apocalyptic wasteland, Max teams up with a mysterious woman to escape a tyrannical warlord."),
            new MovieRecommendation("Baby Driver", "Edgar Wright", 2017, "Action/Crime", 
                "After being coerced into working for a crime boss, a young getaway driver finds himself in a doomed heist.")
        ));
        
        // ANGRY movies
        movies.put(MoodType.ANGRY, Arrays.asList(
            new MovieRecommendation("John Wick", "Chad Stahelski", 2014, "Action/Thriller", 
                "An ex-hit-man comes out of retirement to track down the gangsters that took everything from him."),
            new MovieRecommendation("Kill Bill: Vol. 1", "Quentin Tarantino", 2003, "Action/Crime", 
                "The Bride wakens from a four-year coma and embarks on a roaring rampage of revenge."),
            new MovieRecommendation("Mad Max: Fury Road", "George Miller", 2015, "Action/Adventure", 
                "In a post-apocalyptic wasteland, Max teams up with a mysterious woman to escape a tyrannical warlord."),
            new MovieRecommendation("The Raid", "Gareth Evans", 2011, "Action/Thriller", 
                "A S.W.A.T. team becomes trapped in a tenement run by a ruthless mobster and his army of killers."),
            new MovieRecommendation("Oldboy", "Park Chan-wook", 2003, "Action/Drama", 
                "After being kidnapped and imprisoned for fifteen years, a man is released and must find his captor.")
        ));
        
        // CONTENT movies
        movies.put(MoodType.CONTENT, Arrays.asList(
            new MovieRecommendation("The Shawshank Redemption", "Frank Darabont", 1994, "Drama", 
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption."),
            new MovieRecommendation("Forrest Gump", "Robert Zemeckis", 1994, "Drama/Comedy", 
                "The presidencies of Kennedy and Johnson through the eyes of an Alabama man with an IQ of 75."),
            new MovieRecommendation("Life is Beautiful", "Roberto Benigni", 1997, "Comedy/Drama", 
                "When an open-minded Jewish librarian and his son become victims of the Holocaust, he uses humor to protect his son."),
            new MovieRecommendation("The Pursuit of Happyness", "Gabriele Muccino", 2006, "Drama", 
                "A struggling salesman takes custody of his son as he's poised to begin a life-changing professional career."),
            new MovieRecommendation("About Time", "Richard Curtis", 2013, "Comedy/Drama", 
                "At 21, Tim discovers he can travel in time and change what happens in his life.")
        ));
        
        // TIRED movies
        movies.put(MoodType.TIRED, Arrays.asList(
            new MovieRecommendation("My Neighbor Totoro", "Hayao Miyazaki", 1988, "Animation/Family", 
                "When two girls move to the country, they have adventures with forest spirits."),
            new MovieRecommendation("Amélie", "Jean-Pierre Jeunet", 2001, "Comedy/Romance", 
                "Amélie is an innocent and naive girl in Paris with her own sense of justice."),
            new MovieRecommendation("The Princess Bride", "Rob Reiner", 1987, "Adventure/Comedy", 
                "A bedridden boy's grandfather reads him a story of adventure, true love, and friendship."),
            new MovieRecommendation("Paddington", "Paul King", 2014, "Comedy/Family", 
                "A young Peruvian bear travels to London in search of a home."),
            new MovieRecommendation("Big Fish", "Tim Burton", 2003, "Adventure/Drama", 
                "A frustrated son tries to determine the fact from fiction in his dying father's life.")
        ));
        
        // ENERGETIC movies
        movies.put(MoodType.ENERGETIC, Arrays.asList(
            new MovieRecommendation("The Greatest Showman", "Michael Gracey", 2017, "Musical/Drama", 
                "Celebrates the birth of show business and tells of a visionary who rose from nothing."),
            new MovieRecommendation("La La Land", "Damien Chazelle", 2016, "Musical/Romance", 
                "While navigating their careers in Los Angeles, a pianist and an actress fall in love."),
            new MovieRecommendation("School of Rock", "Richard Linklater", 2003, "Comedy/Music", 
                "After being kicked out of his rock band, a guitarist poses as a substitute teacher."),
            new MovieRecommendation("Moana", "Ron Clements", 2016, "Animation/Adventure", 
                "In Ancient Polynesia, Moana sets sail in search of a fabled island with a demigod."),
            new MovieRecommendation("The Incredibles", "Brad Bird", 2004, "Animation/Action", 
                "A family of undercover superheroes tries to live the quiet suburban life.")
        ));
        
        return movies;
    }
    
    private List<SongRecommendation> getDefaultSongRecommendations() {
        return Arrays.asList(
            new SongRecommendation("Good Vibes", "Various Artists", "Feel Good Hits", "Pop"),
            new SongRecommendation("Peaceful Mind", "Relaxation Music", "Calm Collection", "Ambient"),
            new SongRecommendation("Motivation", "Upbeat Artists", "Energy Boost", "Pop")
        );
    }
    
    private List<MovieRecommendation> getDefaultMovieRecommendations() {
        return Arrays.asList(
            new MovieRecommendation("The Pursuit of Happyness", "Gabriele Muccino", 2006, "Drama", 
                "An inspiring story of perseverance and determination."),
            new MovieRecommendation("Inside Out", "Pete Docter", 2015, "Animation/Family", 
                "A beautiful exploration of emotions and growing up."),
            new MovieRecommendation("The Princess Bride", "Rob Reiner", 1987, "Adventure/Comedy", 
                "A timeless story of adventure, love, and friendship.")
        );
    }
}