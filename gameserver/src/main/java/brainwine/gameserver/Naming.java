package brainwine.gameserver;

/**
 * TODO all I'm doing here is moving the problem somewhere else.
 * 
 * Entity names are sourced from: https://github.com/bytebin/deepworld-gameserver/blob/master/config/fake.yml
 */
public class Naming {
    
    private static final String[] ZONE_FIRST_NAMES = {
        "Malvern", "Tralee", "Horncastle", "Old", "Westwood",
        "Citta", "Tadley", "Mossley", "West", "East",
        "North", "South", "Wadpen", "Githam", "Soatnust",
        "Highworth", "Creakynip", "Upper", "Lower", "Cannock",
        "Dovercourt", "Limerick", "Pickering", "Glumshed", "Crusthack",
        "Osyltyr", "Aberstaple", "New", "Stroud", "Crumclum",
        "Crumsidle", "Bankswund", "Fiddletrast", "Bournpan", "St.",
        "Funderbost", "Bexwoddly", "Pilkingheld", "Wittlepen", "Rabbitbleaker",
        "Griffingumby", "Guilthead", "Bigglelund", "Bunnymold", "Rosesidle",
        "Crushthorn", "Tanlyward", "Ahncrace", "Pilkingking", "Dingstrath",
        "Axebury", "Ginglingtap", "Ballybibby", "Shadehoven"
    };
    
    private static final String[] ZONE_LAST_NAMES = {
        "Falls", "Alloa", "Glen", "Way", "Dolente",
        "Peak", "Heights", "Creek", "Banffshire", "Chagford",
        "Gorge", "Valley", "Catacombs", "Depths", "Mines",
        "Crickbridge", "Guildbost", "Pits", "Vaults", "Ruins",
        "Dell", "Keep", "Chatterdin", "Scrimmance", "Gitwick",
        "Ridge", "Alresford", "Place", "Bridge", "Glade",
        "Mill", "Court", "Dooftory", "Hills", "Specklewint",
        "Grove", "Aylesbury", "Wagwouth", "Russetcumby", "Point",
        "Canyon", "Cranwarry", "Bluff", "Passage", "Crantippy",
        "Kerbodome", "Dale", "Cemetery"
    };
    
    public static final String[] ENTITY_FIRST_NAMES = {
        "Aaron", "Abby", "Abigale", "Abraham", "Ada", "Adella", "Agnes", "Alan", 
        "Albert", "Alexander", "Allie", "Almira", "Almyra", "Alonzo", "Alva", "Ambrose", 
        "Amelia", "Amon", "Amos", "Andrew", "Ann", "Annie", "Aquilla", "Archibald", 
        "Arnold", "Arrah", "Asa", "Augustus", "Barnabas", "Bartholomew", "Beatrice", "Becky", 
        "Benedict", "Benjamin", "Bennet", "Bernard", "Bernice", "Bertram", "Bess", "Bessie", 
        "Beth", "Betsy", "Buford", "Byron", "Calvin", "Charity", "Charles", "Charlotte", 
        "Chastity", "Christopher", "Claire", "Clarence", "Clement", "Clinton", "Cole", "Columbus", 
        "Commodore Perry", "Constance", "Cynthia", "Daniel", "David", "Dick", "Dorothy", "Edith", 
        "Edmund", "Edna", "Edward", "Edwin", "Edwina", "Eldon", "Eleanor", "Eli", 
        "Elijah", "Eliza", "Elizabeth", "Ella", "Ellie", "Elvira", "Emma", "Emmett", 
        "Enoch", "Esther", "Ethel", "Ettie", "Eudora", "Eva", "Ezekiel", "Ezra", 
        "Fanny", "Fidelia", "Flora", "Florence", "Frances", "Francis", "Franklin", "Frederick", 
        "Gabriel", "Garrett", "Geneve", "Genevieve", "George", "George", "Georgia", "Gertie", 
        "Gertrude", "Gideon", "Gilbert", "Ginny", "Gladys", "Grace", "Granville", "Hannah", 
        "Harland", "Harold", "Harrison", "Harvey", "Hattie", "Helen", "Helene", "Henrietta", 
        "Henry", "Hester", "Hettie", "Hiram", "Hope", "Horace", "Horatio", "Hortence", 
        "Hugh", "Isaac", "Isaac Newton", "Isabella", "Isaiah", "Israel", "Jacob", "James", 
        "Jane", "Jasper", "Jedediah", "Jefferson", "Jennie", "Jeptha", "Jessamine", "Jesse", 
        "Joel", "John Paul", "John Wesley", "Jonathan", "Joseph", "Josephine", "Josephus", "Joshua", 
        "Josiah", "Judith", "Julia", "Julian", "Juliet", "Julius", "Katherine", "Lafayette", 
        "Laura", "Lawrence", "Leah", "Leander", "Lenora", "Les", "Letitia", "Levi", 
        "Levi", "Lewis", "Lila", "Lilly", "Liza", "Lorena", "Lorraine", "Lottie", 
        "Louis", "Louisa", "Louise", "Lucas", "Lucas", "Lucian", "Lucian", "Lucius", 
        "Lucius", "Lucy", "Luke", "Luke", "Lulu", "Luther", "Luther", "Lydia", 
        "Mahulda", "Marcellus", "Margaret", "Mark", "Martha", "Martin", "Mary", "Mary Elizabeth", 
        "Mary Frances", "Masheck", "Matilda", "Matthew", "Maude", "Maurice", "Maxine", "Maxwell", 
        "Mercy", "Meriwether", "Meriwether Lewis", "Merrill", "Mildred", "Minerva", "Missouri", "Molly", 
        "Mordecai", "Morgan", "Morris", "Myrtle", "Nancy", "Natalie", "Nathaniel", "Ned", 
        "Nellie", "Nettie", "Newton", "Nicholas", "Nimrod", "Ninian", "Nora", "Obediah", 
        "Octavius", "Orpha", "Orville", "Oscar", "Owen", "Parthena", "Patrick", "Patrick Henry", 
        "Patsy", "Paul", "Paul", "Peggy", "Permelia", "Perry", "Peter", "Philomena", 
        "Phoebe", "Pleasant", "Polly", "Preshea", "Rachel", "Ralph", "Raymond", "Rebecca", 
        "Reuben", "Rhoda", "Richard", "Robert", "Robert Lee", "Roderick", "Rowena", "Rudolph", 
        "Rufina", "Rufus", "Ruth", "Sally", "Sam Houston", "Samantha", "Samuel", "Sarah", 
        "Sarah Ann", "Sarah Elizabeth", "Savannah", "Selina", "Seth", "Silas", "Simeon", "Simon", 
        "Sophronia", "Stanley", "Stella", "Stephen", "Thaddeus", "Theodore", "Theodosia", "Thomas", 
        "Timothy", "Ulysses", "Uriah", "Vertiline", "Victor", "Victoria", "Virginia", "Vivian", 
        "Walter", "Warren", "Washington", "Wilfred", "William", "Winnifred", "Zachariah", "Zebulon", 
        "Zedock", "Zona", "Zylphia"
    };
    
    public static final String[] ENTITY_LAST_NAMES = {
        "Abraham", "Adams", "Alcorn", "Alderdice", "Angus", "Ashdown", "Ayre", "Backhaus", 
        "Baldwin", "Bamford", "Beaton", "Blackwood", "Blair", "Blewett", "Bornholdt", "Bowden", 
        "Burrows", "Cameron", "Carroll", "Clarke", "Claxton", "Collins", "Colson", "Connor", 
        "Conroy", "Cullen", "Cunningham", "Curd", "Curnow", "Cusack", "Dagon", "Dalton", 
        "Dawes", "Desmond", "Dewar", "Dickenson", "Donnell", "Drummond", "Dunstan", "English", 
        "Eveans", "Faraday", "Faulkner", "Fitzgerald", "Fitzpatrick", "Fletcher", "Foster", "Franklin", 
        "Fulton", "Gallagher", "Gibbons", "Gilmore", "Glover", "Goodfellow", "Goodwin", "Griffiths", 
        "Gullifer", "Hadley", "Haeffner", "Hanlon", "Harding", "Harris", "Holloway", "Hughes", 
        "Jarvis", "Jefferies", "Johnstone", "Kaylock", "Keane", "Kemp", "Kernaghan", "Kirby", 
        "Kirkland", "Knight", "LaFontaine", "Lawford", "Lawrence", "Lennox", "Longley", "Lonsdale", 
        "Luckett", "Lyons", "Macklin", "Madill", "Marsden", "Marshall", "Martin", "Mather", 
        "Mathieson", "Maunder", "McColl", "McDermott", "McGillicuddy", "McKenzie", "McLachlan", "McNeil", 
        "Meaklim", "Meighan", "Mellor", "Meyers", "Milsom", "Mitchell", "Mitchelson", "Moore", 
        "Morgan", "Morrison", "Mortimer", "Moulsdale", "Murphy", "Nelson", "Nolan", "Noonan", 
        "O'Keefe", "O'Sullivan", "Palmer", "Parnell", "Pattison", "Pettit", "Phillips", "Pinner", 
        "Porter", "Prosser", "Ramseyer", "Renton", "Rickard", "Riddington", "Roche", "Rowe", 
        "Russell", "Salisbury", "Saunders", "Sawyer", "Scanlan", "Scarborough", "Schwarer", "Sheary", 
        "Sheedy", "Shelton", "Shields", "Shinnick", "Skinner", "Sommer", "Spencer", "Stanbury", 
        "Stanton", "Storey", "Swaisbrick", "Thorley", "Thumpston", "Tichborne", "Tinning", "Tobin", 
        "Todd", "Trimble", "Twomey", "Upton", "Urwin", "Vandenburg", "Vinge", "Wakefield", 
        "Wakenshaw", "Walden", "Wallace", "Walton", "Warner", "Webb", "Whitehill", "Wickes", 
        "Wilberforce", "Wilkinson", "Wolstenholme", "Wright"
    };
    
    public static String getRandomZoneName() {
        return getRandomName(ZONE_FIRST_NAMES, ZONE_LAST_NAMES);
    }
    
    public static String getRandomEntityName() {
        return getRandomName(ENTITY_FIRST_NAMES, ENTITY_LAST_NAMES);
    }
    
    private static String getRandomName(String[] firstNames, String[] lastNames) {
        String firstName = firstNames[(int)(Math.random() * firstNames.length)];
        String lastName = lastNames[(int)(Math.random() * lastNames.length)];
        return firstName + " " + lastName;
    }
}
