// FamousCinemasData.kt
// צור קובץ חדש: app/src/main/java/com/example/mycinema/data/FamousCinemasData.kt
package com.example.mycinema.data

import com.example.mycinema.models.Cinema

object FamousCinemasData {

    fun getFamousCinemas(): List<Cinema> = listOf(

        // ישראל (5 בתי קולנוע)
        Cinema(101, "לב סינמטק תל אביב", "שדרות שאול המלך 2, תל אביב", 32.0719, 34.7856, "03-6060800", "https://www.lev.org.il", null, 4.5f),
        Cinema(102, "סינמה סיטי גלילות", "קניון גלילות, תל אביב", 32.0668, 34.7924, "03-6428888", "https://www.cinema-city.co.il", null, 4.2f),
        Cinema(103, "YES פלאנט כפר סבא", "ויצמן 130, כפר סבא", 32.1743, 34.9073, "09-7676777", "https://www.yesplanet.co.il", null, 4.1f),
        Cinema(104, "הוט סינמה אשדוד", "יפה נוף 1, אשדוד", 31.7957, 34.6516, "08-8567777", null, null, 4.0f),
        Cinema(105, "רב חן ירושלים", "יפו 8, ירושלים", 31.7767, 35.2186, "02-6221333", null, null, 4.3f),

        // ארצות הברית (5 בתי קולנוע)
        Cinema(106, "TCL Chinese Theatre", "6925 Hollywood Blvd, Los Angeles", 34.1022, -118.3408, "+1-323-464-8111", "https://www.tclchinesetheatres.com", null, 4.4f),
        Cinema(107, "AMC Empire 25", "234 W 42nd St, New York", 40.7589, -73.9851, "+1-212-398-3939", "https://www.amctheatres.com", null, 4.1f),
        Cinema(108, "El Capitan Theatre", "6838 Hollywood Blvd, Los Angeles", 34.1016, -118.3365, "+1-800-347-6396", "https://elcapitantheatre.com", null, 4.6f),
        Cinema(109, "Cinerama Dome", "6360 Sunset Blvd, Los Angeles", 34.0978, -118.3267, null, null, null, 4.3f),
        Cinema(110, "Radio City Music Hall", "1260 Avenue of the Americas, New York", 40.7590, -73.9799, "+1-212-247-4777", "https://www.radiocity.com", null, 4.5f),

        // לונדון, בריטניה (4 בתי קולנוע)
        Cinema(111, "BFI IMAX", "1 Charlie Chaplin Walk, London", 51.5067, -0.1139, "+44-20-7928-3232", "https://www.bfi.org.uk", null, 4.3f),
        Cinema(112, "Odeon Leicester Square", "24-26 Leicester Square, London", 51.5112, -0.1299, "+44-871-224-4007", "https://www.odeon.co.uk", null, 4.2f),
        Cinema(113, "Prince Charles Cinema", "7 Leicester Pl, London", 51.5115, -0.1297, "+44-20-7494-3654", "https://princecharlescinema.com", null, 4.4f),
        Cinema(114, "Barbican Cinema", "Silk St, London", 51.5203, -0.0939, "+44-20-7638-8891", "https://www.barbican.org.uk", null, 4.1f),

        // פריז, צרפת (3 בתי קולנוע)
        Cinema(115, "Grand Rex", "1 Boulevard Poissonnière, Paris", 48.8711, 2.3478, "+33-1-45-08-93-58", "https://www.legrandrex.com", null, 4.5f),
        Cinema(116, "Cinémathèque Française", "51 Rue de Bercy, Paris", 48.8368, 2.3828, "+33-1-71-19-33-33", "https://www.cinematheque.fr", null, 4.4f),
        Cinema(117, "MK2 Bibliothèque", "128 Avenue de France, Paris", 48.8297, 2.3766, "+33-892-69-84-84", "https://www.mk2.com", null, 4.0f),

        // ברלין, גרמניה (3 בתי קולנוע)
        Cinema(118, "Zoo Palast", "Hardenbergstraße 29A, Berlin", 52.5066, 13.3337, "+49-30-254-69-0", "https://www.zoopalast.de", null, 4.3f),
        Cinema(119, "Kino International", "Karl-Marx-Allee 33, Berlin", 52.5156, 13.4378, "+49-30-2473-2011", null, null, 4.2f),
        Cinema(120, "Arsenal", "Potsdamer Str. 2, Berlin", 52.5096, 13.3683, "+49-30-26955100", "https://www.arsenal-berlin.de", null, 4.4f),

        // טוקיו, יפן (3 בתי קולנוע)
        Cinema(121, "Toho Cinemas Roppongi Hills", "6-10-2 Roppongi, Tokyo", 35.6606, 139.7298, "+81-3-5775-6090", "https://www.tohotheater.jp", null, 4.2f),
        Cinema(122, "Tokyo International Forum", "3-5-1 Marunouchi, Tokyo", 35.6763, 139.7630, "+81-3-5221-9000", "https://www.t-i-forum.co.jp", null, 4.1f),
        Cinema(123, "Shibuya Sky", "2-24-12 Shibuya, Tokyo", 35.6580, 139.7016, null, null, null, 4.0f),

        // הונג קונג (2 בתי קולנוע)
        Cinema(124, "UA IMAX at Airport Core", "Terminal 2, Hong Kong Airport", 22.3080, 113.9185, "+852-2317-6666", null, null, 4.1f),
        Cinema(125, "The Grand Cinema", "Elements Mall, Kowloon", 22.3042, 114.1657, "+852-2735-8888", null, null, 4.2f),

        // סידני, אוסטרליה (2 בתי קולנוע)
        Cinema(126, "State Theatre", "49 Market St, Sydney", -33.8688, 151.2093, "+61-2-9373-6555", "https://www.statetheatre.com.au", null, 4.4f),
        Cinema(127, "IMAX Sydney", "31 Wheat Rd, Sydney", -33.8688, 151.2000, "+61-2-9213-7000", "https://www.imax.com.au", null, 4.3f),

        // ריו דה ז'נרו, ברזיל (2 בתי קולנוע)
        Cinema(128, "Kinoplex Leblon", "Av. Ataulfo de Paiva, Rio de Janeiro", -22.9838, -43.2196, "+55-21-2540-8820", null, null, 4.0f),
        Cinema(129, "UCI New York City Center", "Av. das Américas, Rio de Janeiro", -23.0205, -43.3137, "+55-21-3431-9999", null, null, 3.9f),

        // מקסיקו סיטי, מקסיקו (2 בתי קולנוע)
        Cinema(130, "Cinemex Casa de Arte", "Av. Revolución, Mexico City", 19.3318, -99.1840, "+52-55-5262-4000", "https://www.cinemex.com", null, 4.1f),
        Cinema(131, "Cinépolis Plaza Universidad", "Av. Copilco, Mexico City", 19.3241, -99.1781, "+52-55-5481-8900", "https://www.cinepolis.com", null, 4.0f),

        // מומבאי, הודו (2 בתי קולנוע)
        Cinema(132, "Regal Cinema", "Colaba Causeway, Mumbai", 18.9067, 72.8147, "+91-22-2202-1017", null, null, 3.8f),
        Cinema(133, "INOX Megaplex", "R City Mall, Mumbai", 19.1057, 72.8274, "+91-22-6145-6145", "https://www.inoxmovies.com", null, 4.0f),

        // בוארוס איירס, ארגנטינה (2 בתי קולנוע)
        Cinema(134, "Atlas Lavalle", "Lavalle 869, Buenos Aires", -34.6021, -58.3845, "+54-11-4322-1195", null, null, 4.2f),
        Cinema(135, "Hoyts Abasto", "Av. Corrientes 3247, Buenos Aires", -34.6037, -58.4111, "+54-11-4959-3200", "https://www.hoyts.com.ar", null, 4.0f),

        // קייפטאון, דרום אפריקה (2 בתי קולנוע)
        Cinema(136, "Labia Theatre", "68 Orange St, Cape Town", -33.9249, 18.4241, "+27-21-424-5927", "https://www.thelabia.co.za", null, 4.3f),
        Cinema(137, "Nu Metro V&A Waterfront", "Dock Rd, Cape Town", -33.9024, 18.4179, "+27-861-100-700", "https://www.numetro.co.za", null, 4.1f),

        // מוסקבה, רוסיה (2 בתי קולנוע)
        Cinema(138, "35mm Cinema", "Pokrovka St, Moscow", 55.7558, 37.6176, "+7-495-916-9560", null, null, 4.2f),
        Cinema(139, "Oktyabr Cinema", "Novy Arbat 24, Moscow", 55.7522, 37.5911, "+7-495-789-2435", null, null, 4.0f),

        // בנגקוק, תאילנד (2 בתי קולנוע)
        Cinema(140, "House Samyan", "Samyan Mitrtown, Bangkok", 13.7307, 100.5418, "+66-2-160-5000", null, null, 4.2f),
        Cinema(141, "SF Cinema City", "CentralWorld, Bangkok", 13.7467, 100.5396, "+66-2-268-8888", null, null, 4.0f),

        // סיגפור (2 בתי קולנוע)
        Cinema(142, "The Cathay Cineleisure", "68 Orchard Rd, Singapore", 1.3006, 103.8397, "+65-6235-1155", "https://www.cathaycineplexes.com.sg", null, 4.1f),
        Cinema(143, "Shaw Theatres Lido", "350 Orchard Rd, Singapore", 1.3067, 103.8310, "+65-6738-7766", "https://www.shaw.sg", null, 4.0f),

        // דובאי, איחוד האמירויות (2 בתי קולנוע)
        Cinema(144, "Reel Cinemas Dubai Mall", "Financial Centre Rd, Dubai", 25.1975, 55.2796, "+971-4-449-1988", "https://www.reelcinemas.ae", null, 4.2f),
        Cinema(145, "VOX Cinemas Mall of the Emirates", "Sheikh Zayed Rd, Dubai", 25.1181, 55.2008, "+971-600-599-905", "https://www.voxcinemas.com", null, 4.1f),

        // איסטנבול, טורקיה (2 בתי קולנוע)
        Cinema(146, "Rexx Cinema", "İstiklal Cd, Istanbul", 41.0339, 28.9784, "+90-212-293-2169", null, null, 4.0f),
        Cinema(147, "Cinemaximum", "Zorlu Center, Istanbul", 41.0658, 29.0136, "+90-444-0-269", "https://www.cinemaximum.com.tr", null, 4.1f),

        // קהיר, מצרים (2 בתי קולנוע)
        Cinema(148, "Galaxy Cinema", "City Centre Almaza, Cairo", 30.0975, 31.3370, "+20-2-2480-0123", null, null, 3.9f),
        Cinema(149, "IMAX First Mall", "35th Ring Rd, Cairo", 30.0626, 31.2497, "+20-2-2536-0000", null, null, 4.0f),

        // לאגוס, ניגריה (1 בית קולנוע)
        Cinema(150, "Silverbird Cinemas", "Ahmadu Bello Way, Lagos", 6.4318, 3.4219, "+234-1-270-1040", "https://silverbirdcinemas.com", null, 3.8f)
    )

    /**
     * קבלת בתי קולנוע לפי מדינה
     */
    fun getCinemasByCountry(country: String): List<Cinema> {
        return when (country.lowercase()) {
            "israel" -> getFamousCinemas().filter { it.id in 101..105 }
            "usa", "united states" -> getFamousCinemas().filter { it.id in 106..110 }
            "uk", "britain", "england" -> getFamousCinemas().filter { it.id in 111..114 }
            "france" -> getFamousCinemas().filter { it.id in 115..117 }
            "germany" -> getFamousCinemas().filter { it.id in 118..120 }
            "japan" -> getFamousCinemas().filter { it.id in 121..123 }
            "hong kong" -> getFamousCinemas().filter { it.id in 124..125 }
            "australia" -> getFamousCinemas().filter { it.id in 126..127 }
            "brazil" -> getFamousCinemas().filter { it.id in 128..129 }
            "mexico" -> getFamousCinemas().filter { it.id in 130..131 }
            "india" -> getFamousCinemas().filter { it.id in 132..133 }
            "argentina" -> getFamousCinemas().filter { it.id in 134..135 }
            "south africa" -> getFamousCinemas().filter { it.id in 136..137 }
            "russia" -> getFamousCinemas().filter { it.id in 138..139 }
            "thailand" -> getFamousCinemas().filter { it.id in 140..141 }
            "singapore" -> getFamousCinemas().filter { it.id in 142..143 }
            "uae", "dubai" -> getFamousCinemas().filter { it.id in 144..145 }
            "turkey" -> getFamousCinemas().filter { it.id in 146..147 }
            "egypt" -> getFamousCinemas().filter { it.id in 148..149 }
            "nigeria" -> getFamousCinemas().filter { it.id == 150 }
            else -> getFamousCinemas()
        }
    }

    /**
     * קבלת בתי קולנוע בקרבת קואורדינטה
     */
    fun getCinemasNear(latitude: Double, longitude: Double, radiusKm: Double = 50.0): List<Cinema> {
        return getFamousCinemas().filter { cinema ->
            val distance = calculateDistance(latitude, longitude, cinema.latitude, cinema.longitude)
            distance <= radiusKm
        }.sortedBy { cinema ->
            calculateDistance(latitude, longitude, cinema.latitude, cinema.longitude)
        }
    }

    /**
     * חישוב מרחק בין שתי נקודות (בקילומטרים)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // רדיוס כדור הארץ בקילומטרים
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}