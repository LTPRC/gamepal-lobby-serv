package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.config.GamePalConstants;
import org.springframework.util.StringUtils;

import java.util.Random;

public class NameUtil {

    private NameUtil() {}

    protected static final String[] chineseLastnames = new String[] { "王", "李", "张", "刘", "陈", "杨", "黄", "吴", "赵", "周",
            "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "高", "罗", "郑", "梁", "谢", "宋", "唐", "许", "邓", "冯", "韩", "曹", "彭",
            "曾", "肖", "田", "董", "潘", "袁", "蔡", "蒋", "余", "于", "杜", "叶", "程", "魏", "苏", "吕", "丁", "任", "卢", "姚", "沈",
            "钟", "姜", "崔", "谭", "陆", "范", "汪", "廖", "石", "金", "韦", "贾", "夏", "付", "方", "邹", "熊", "白", "孟", "秦", "邱",
            "侯", "江", "尹", "薛", "闫", "段", "雷", "龙", "黎", "史", "陶", "毛", "贺", "郝", "顾", "龚", "邵", "万", "覃", "武", "钱",
            "戴", "严", "欧", "莫", "孔", "向" };
    protected static final int[] chineseLastnameWeights = new int[] { 794, 741, 707, 538, 453, 308, 229, 223, 212, 205,
            173, 152, 131, 126, 121, 118, 117, 115, 105, 86, 84, 81, 78, 72, 68, 65, 64, 62, 61, 59, 57, 57, 54, 54, 54,
            51, 50, 50, 49, 47, 47, 47, 47, 46, 45, 42, 42, 42, 42, 41, 41, 41, 40, 39, 39, 38, 37, 37, 37, 36, 36, 36,
            35, 35, 34, 34, 33, 32, 32, 31, 30, 29, 29, 28, 28, 27, 27, 26, 26, 25, 25, 25, 24, 24, 24, 24, 23, 23, 22,
            19, 19, 19, 19, 18, 18, 18, 18, 18, 17, 17 };
    protected static final String[] chineseMaleFirstnames = new String[] { "英", "华", "玉", "秀", "文", "明", "兰", "金", "国",
            "春", "丹", "忠", "小", "梓", "云", "方", "平", "海", "正", "荣", "雨", "宇", "航", "之", "天", "可", "吉", "子", "豪", "辛",
            "易", "于", "昊", "浩", "杰", "铁", "捷", "云", "峰", "信", "龙", "虎", "林", "功", "乔", "笑", "一", "立", "涛", "诚", "韬",
            "东", "南", "远", "琦", "琪", "思", "楠", "亚", "泽", "熙", "若", "江", "兴", "星", "幸", "喜", "秋", "夏", "光", "依", "诺",
            "波", "澜", "宝", "玉", "帅", "松", "群", "柏", "鸿", "洪", "宏", "金", "建", "中", "奇", "乐", "风", "驰", "锋", "利", "伟",
            "星", "辰", "晨", "文", "刚", "京", "晶", "敬", "志", "靖", "阳", "畅", "昌", "巍", "俊", "君", "军" };
    protected static final String[] chineseFemaleFirstnames = new String[] { "英", "华", "玉", "秀", "文", "明", "兰", "金", "丹",
            "春", "丽", "红", "小", "梅", "云", "芳", "平", "海", "珍", "荣", "雨", "宇", "梓", "莉", "天", "可", "吉", "子", "之", "欣",
            "怡", "愉", "昊", "辛", "杰", "洁", "捷", "云", "易", "信", "雪", "晴", "林", "于", "乔", "笑", "一", "立", "秋", "诚", "夏",
            "东", "南", "远", "琦", "琪", "思", "楠", "亚", "泽", "熙", "若", "江", "兴", "星", "幸", "喜", "婉", "如", "光", "依", "诺",
            "波", "澜", "宝", "玉", "帅", "娜", "艳", "燕", "群", "祎", "玢", "金", "伊", "中", "奇", "乐", "风", "驰", "歆", "利", "伟",
            "星", "辰", "晨", "文", "静", "京", "晶", "敬", "婧", "靖", "婕", "畅", "昌", "菁", "萌", "君", "梦" };
    protected static final String[] japaneseLastnames = new String[] { "佐藤", "铃木", "高桥", "田中", "伊藤", "渡边", "山本", "中村",
            "小林", "加藤", "吉田", "山田", "佐佐木", "山口", "松本", "井上", "木村", "林", "斋藤", "清水", "山崎", "阿部", "森", "池田", "桥本", "山下",
            "石川", "中岛", "前田", "藤田", "三濑", "梶村", "额贺", "下坂", "永仓", "井伊", "樋川", "多和田", "大渊", "森中", "矢田部", "白须", "上井",
            "玉山", "帆足", "真砂", "宇高", "土师", "竹上", "大成", "桑本", "阴山", "筱田", "田添", "松岛", "苍井", "武藤", "小泽", "三上", "坂田", "龟田",
            "工藤", "藤崎", "堀咲", "水咲", "泷泽", "本多", "本田", "丰田", "刚田", "骨川", "野比", "源", "小岛", "毛利", "上原", "八神", "泉", "石田",
            "高石", "武之内", "城户", "太刀川", "新垣", "东条", "向井", "青木", "滨崎", "吉泽", "波多野", "花野", "麻生", "小泉", "菅", "鸠山", "德田",
            "乃木", "羽田", "宇都宫", "服部" };
    protected static final String[] japaneseMaleFirstnames = new String[] { "清", "勇", "茂", "博", "実", "进", "弘", "正", "胜",
            "隆", "翔太", "莲", "翼", "陆", "飒太", "奏太", "拓海", "健太", "大和", "大辅", "隼人", "阳斗", "悠太", "翔", "谅", "大地", "悠斗", "响",
            "海斗", "悠", "诚", "太一", "骏", "一郎", "优斗", "匠", "树", "凉太", "空", "直树", "太郎", "优", "达也", "大辉", "龙之介", "润", "太阳",
            "健太郎", "拓真", "大翔", "优希", "大驾", "一辉", "大树", "拓也", "光", "凛", "凉", "直人", "和真", "枫", "伊织", "千太", "聪", "雄太",
            "一真", "朝阳", "健一", "圭吾", "春树", "庆", "新", "新一", "龙", "龙一", "健人", "晴", "纯一郎", "史郎", "次郎", "由纪夫", "康夫", "刚昌",
            "光子郎", "丈", "武", "元太", "光彦", "健", "大木", "和也", "达也", "藤吉", "二郎丸", "海", "令", "正男", "贤", "京", "英夫", "昭夫", "和志",
            "良一", "隆", "和男", "久", "英才", "英明", "瞳", "凉介" };
    protected static final String[] japaneseFemaleFirstnames = new String[] { "莲", "翼", "翔", "空", "优", "光", "兰", "园子", "凛",
            "凉", "枫", "伊织", "朝阳", "晴", "夏树", "晴子", "里子", "美智子", "智美", "爱", "秀子", "明美", "步美", "优子", "英子", "妙子", "由美",
            "知佳子", "叶子", "郁子", "香织", "绫子", "瞳", "明子", "里美", "幸", "晃子", "洋子", "益子", "光子", "裕子", "美菜", "明日香", "优希", "舞",
            "结衣", "亚衣", "瑞穗", "咲", "美雪", "一杏", "麻衣", "杏梨", "美莉", "唯香", "美凉", "恋", "南", "悠亚", "佳奈", "香织", "有菜", "百合",
            "一花", "杏", "雅美", "百惠", "丽", "心音", "香奈", "莉乃", "明里", "彩", "澜", "宝", "玉", "帅", "娜", "艳", "燕", "群", "祎", "玢",
            "金", "伊", "中", "奇", "乐", "风", "驰", "歆", "利", "伟", "星", "遥", "明", "萌", "步", "优", "明", "莉亚", "稀", "纱", "千子",
            "恵美", "真凛", "真央", "佳纯", "梦露", "直美" };
    protected static final String[] internationalLastnames = new String[] { "Smith", "Lopez", "Williams", "Joseph",
            "Hernandez", "Charles", "Rodriguez", "Brown", "Andersson", "Hansen", "Ivanov", "Murphy", "Martin", "Muller",
            "Nowak", "Rossi", "Yilmaz", "Garcia", "Silva", "Ivanova", "Kim", "Mohamed", "Devi", "Ali", "Khan", "Saidi",
            "Da Silva", "Gonzalez", "Johnson", "Jones", "Miller", "Davis", "Martinez", "Wilson", "Thomas", "Taylor",
            "Lee", "Thomposon", "Perez", "Harris", "White", "Sanchez", "Clark", "Lewis", "Allen", "Young", "King",
            "Wright", "Torres", "Scott", "Green", "Adams", "Flores", "Nelson", "Baker", "Hall", "Campbell", "Roberts",
            "Carter", "Gomez", "Turner", "Parker", "Turner", "Moore", "Jackson", "Robinson", "Hill", "Mitchell",
            "Phillips", "Evans", "Diaz", "Cruz", "Edwards", "Morris", "Cook", "Morgan", "Peterson", "Cooper", "Reed",
            "Kelly", "Howard", "Watson", "Richardson", "Brooks", "Wood", "Hughes", "Sanders", "Foster", "Ross", "Patel",
            "Singh", "Kumar", "Das", "Kaur", "Ram", "Yadav", "Hakim", "Hariri", "Hasan", "Hashim" };
    protected static final String[] internationalMaleFirstnames = new String[] { "Robert", "John", "James", "Michael",
            "William", "David", "Richard", "Joseph", "Charles", "Christopher", "Daniel", "Matthew", "Anthony", "Mark",
            "Donald", "Steven", "Paul", "Andrew", "Joshua", "Kenneth", "Kevin", "Brian", "George", "Edward", "Ronald",
            "Timothy", "Jason", "Jeffrey", "Ryan", "Jacob", "Gary", "Nicholas", "Eric", "Jonathan", "Stephen", "Larry",
            "Justin", "Scott", "Brandon", "Benjamin", "Samuel", "Gregory", "Frank", "Alexander", "Jack", "Raymond",
            "Patrick", "Dennis", "Jerry", "Tyler", "Aaron", "Jose", "Adam", "Henry", "Nathan", "Douglas", "Zachary",
            "Peter", "Kyle", "Walter", "Ethan", "Jeremy", "Harold", "Roger", "Christian", "Keith", "Noah", "Gerald",
            "Carl", "Terry", "Sean", "Austin", "Arthur", "Lawrence", "Jesse", "Dylan", "Bryan", "Joe", "Jordan",
            "Billy", "Bruce", "Albert", "Willie", "Alan", "Logan", "Ralph", "Roy", "Vincent", "Louis", "Philip",
            "Johnny", "Muhammad", "Ram", "Sunita", "Abdul", "Amir", "Adil", "Adnan", "Ahmad", "Ajmal", "Akeem",
            "Artyom", "Mikhail", "Ivan", "Dmitriy", "Leon", "Javier", " Pierre", "Leonardo", "Mehmet" };
    protected static final String[] internationalFemaleFirstnames = new String[] { "Mary", "Patricia", "Jennifer", "Linda",
            "Elizabeth", "Barbara", "Jessica", "Susan", "Laura", "Sharon", "Rebecca", "Stephanie", "Deborah", "Melissa",
            "Amanda", "Carol", "Dorothy", "Michelle", "Donna", "Emily", "Kimberly", "Ashley", "Sandra", "Margaret",
            "Betty", "Lisa", "Nancy", "Karen", "Sarah", "Julie", "Maria", "Ruth", "Janet", "Carolyn", "Catherine",
            "Christine", "Rachel", "Debra", "Katherine ", "Samantha", "Emma", "Brenda", "Nicole", "Anna", "Helen",
            "Angela", "Shirley", "Amy", "Kathleen", "Cynthia", "Jean", "Alice", "Sara", "Kathryn", "Teresa", "Gloria",
            "Ann", "Jacqueline", "Martha", "Hannah", "Andrea", "Cheryl", "Megan", "Judith", "Evelyn", "Joan",
            "Christina", "Lauren", "Kelly", "Olivia", "Joyce", "Kiara", "Aadya", "Saanvi", "Summer", "Renee", "Kayla",
            "Alexis", "Rose", "Charlotte", "Isabella", "Natalie", "Diana", "Marie", "Sophia", "Theresa", "Grace",
            "Amber", "Judy", "Julia", "Abigail", "Doris", "Natalia", "Fernanda", "Camila", "Paula", "Lucía", "Carmen",
            "Vivian", "Mariya", "Fatma", "Jade", "Aisha", "Amal", "Calla", "Aaliyah", "Amara", "Sri", "Fatima",
            "Ananya" };

    public static String[] generateNames() {
        return generateNames("", "");
    }

    public static String[] generateNames(String origin, String gender) {
        if (StringUtils.hasText(origin) || (!GamePalConstants.ORIGIN_CHINESE.equals(origin)
                && !GamePalConstants.ORIGIN_JAPANESE.equals(origin)
                && !GamePalConstants.ORIGIN_INTERNATIONAL.equals(origin))) {
            origin = generateOrigin();
        }
        if (StringUtils.hasText(gender) || (!GamePalConstants.GENDER_MALE.equals(gender)
                && !GamePalConstants.GENDER_FEMALE.equals(gender))) {
            gender = generateGender();
        }
        switch (origin) {
            case GamePalConstants.ORIGIN_CHINESE:
                return generateChineseNames(gender);
            case GamePalConstants.ORIGIN_JAPANESE:
                return generateJapaneseNames(gender);
            case GamePalConstants.ORIGIN_INTERNATIONAL:
                return generateInternationalNames(gender);
            default:
                return new String[] { "", "佚名", "佚名" };
        }
    }

    public static String generateOrigin() {
        double r = Math.random();
        String origin;
        if (r < 0.9D) {
            origin = GamePalConstants.ORIGIN_CHINESE;
        } else if (r < 0.92D) {
            origin = GamePalConstants.ORIGIN_JAPANESE;
        } else {
            origin = GamePalConstants.ORIGIN_INTERNATIONAL;
        }
        return origin;
    }

    public static String generateGender() {
        String gender;
        double r = Math.random();
        if (r < 0.5D) {
            gender = GamePalConstants.GENDER_MALE;
        } else {
            gender = GamePalConstants.GENDER_FEMALE;
        }
        return gender;
    }

    public static String[] generateChineseNames(String gender) {
        String lastName = generateChineseLastname();
        String firstName = generateChineseFirstname(gender);
        double r = Math.random();
        if (r < 0.01D) {
            return new String[] { firstName + firstName, lastName, lastName + firstName + firstName };
        } else if (r < 0.5D) {
            return new String[] { firstName, lastName, lastName + firstName };
        } else {
            String firstName2 = generateChineseFirstname(gender);
            return new String[] { firstName + firstName2, lastName, lastName + firstName + firstName2 };
        }
    }

    public static String generateChineseLastname() {
        Random random = new Random();
        int num = random.nextInt(10000);
        for (int i = 0; i < chineseLastnames.length; i++) {
            if (num < chineseLastnameWeights[i]) {
                return chineseLastnames[i];
            } else {
                num -= chineseLastnameWeights[i];
            }
        }
        return chineseLastnames[random.nextInt(100)];
    }

    public static String generateChineseFirstname(String gender) {
        Random random = new Random();
        int num = random.nextInt(100);
        if (GamePalConstants.GENDER_FEMALE.equals(gender)) {
            return chineseFemaleFirstnames[num];
        } else {
            return chineseMaleFirstnames[num];
        }
    }

    public static String[] generateJapaneseNames(String gender) {
        String lastName = generateJapaneseLastname();
        String firstName = generateJapaneseFirstname(gender);
        return new String[] { firstName, lastName, lastName + firstName };
    }

    public static String generateJapaneseLastname() {
        Random random = new Random();
        int num = random.nextInt(100);
        return japaneseLastnames[num];
    }

    public static String generateJapaneseFirstname(String gender) {
        Random random = new Random();
        int num = random.nextInt(100);
        if (GamePalConstants.GENDER_FEMALE.equals(gender)) {
            return japaneseFemaleFirstnames[num];
        } else {
            return japaneseMaleFirstnames[num];
        }
    }

    public static String[] generateInternationalNames(String gender) {
        String lastName = generateInternationalLastname();
        String firstName = generateInternationalFirstname(gender);
        return new String[] { firstName, lastName, firstName + "·" + lastName };
    }

    public static String generateInternationalLastname() {
        Random random = new Random();
        int num = random.nextInt(100);
        return internationalLastnames[num];
    }

    public static String generateInternationalFirstname(String gender) {
        Random random = new Random();
        int num = random.nextInt(100);
        if (GamePalConstants.GENDER_FEMALE.equals(gender)) {
            return internationalFemaleFirstnames[num];
        } else {
            return internationalMaleFirstnames[num];
        }
    }

    public static String generateSkinColorByOrigin(String origin) {
        double r = Math.random();
        int rst;
        switch (origin) {
            case GamePalConstants.ORIGIN_CHINESE:
                if (r < 0.01D) {
                    rst = GamePalConstants.SKIN_COLOR_B;
                } else if (r < 0.03D) {
                    rst = GamePalConstants.SKIN_COLOR_C;
                } else if (r < 0.1D) {
                    rst = GamePalConstants.SKIN_COLOR_L;
                } else if (r < 0.2D) {
                    rst = GamePalConstants.SKIN_COLOR_M;
                } else {
                    rst = GamePalConstants.SKIN_COLOR_A;
                }
                break;
            case GamePalConstants.ORIGIN_JAPANESE:
                if (r < 0.01D) {
                    rst = GamePalConstants.SKIN_COLOR_C;
                } else if (r < 0.03D) {
                    rst = GamePalConstants.SKIN_COLOR_B;
                } else if (r < 0.1D) {
                    rst = GamePalConstants.SKIN_COLOR_L;
                } else if (r < 0.2D) {
                    rst = GamePalConstants.SKIN_COLOR_M;
                } else {
                    rst = GamePalConstants.SKIN_COLOR_A;
                }
                break;
            case GamePalConstants.ORIGIN_INTERNATIONAL:
            default:
                if (r < 0.2D) {
                    rst = GamePalConstants.SKIN_COLOR_C;
                } else if (r < 0.4D) {
                    rst = GamePalConstants.SKIN_COLOR_B;
                } else if (r < 0.6D) {
                    rst = GamePalConstants.SKIN_COLOR_A;
                } else if (r < 0.8D) {
                    rst = GamePalConstants.SKIN_COLOR_M;
                } else {
                    rst = GamePalConstants.SKIN_COLOR_L;
                }
                break;
        }
        return String.valueOf(rst);
    }

    public static String generateHairStyleByGender(String gender) {
        int rst;
        switch (gender) {
            case GamePalConstants.GENDER_MALE:
                rst = (int) (Math.floor(Math.random() * 6) + 1);
                break;
            case GamePalConstants.GENDER_FEMALE:
                rst = (int) (Math.floor(Math.random() * 6) + 7);
                break;
            default:
                rst = (int) (Math.floor(Math.random() * 12) + 1);
                break;
        }
        return String.valueOf(rst);
    }
}
