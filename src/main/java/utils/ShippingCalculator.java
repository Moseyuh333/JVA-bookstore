package utils;

import models.Shipper;
import models.ShippingQuote;
import models.UserAddress;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods to resolve the most suitable shipping partner and fee for a given address.
 */
public final class ShippingCalculator {

    private static final Map<String, String> PROVINCE_REGION_MAP = createProvinceRegionMap();
    private static final Map<String, String> REGION_ALIAS_MAP = createRegionAliasMap();
    private static final Set<String> GLOBAL_KEYWORDS = createGlobalKeywords();
    private static final List<String> ADMIN_PREFIXES = Arrays.asList(
            "quan", "huyen", "thi xa", "thi tran", "xa", "phuong", "tp", "thanh pho", "tinh", "do thi");
    private static final Set<String> SUPPORTED_COUNTRIES = new HashSet<>(Arrays.asList(
            "viet nam",
            "vietnam",
            "việt nam",
            "vn"
    ));

    private ShippingCalculator() {
    }

    public static ShippingQuote calculateQuote(UserAddress address, List<Shipper> shippers) {
        if (address == null || shippers == null || shippers.isEmpty()) {
            return null;
        }

        AddressContext context = new AddressContext(address);
        if (!context.isSupportedCountry()) {
            return null;
        }

        ShippingQuote best = null;
        for (Shipper shipper : shippers) {
            ShippingQuote candidate = buildQuote(shipper, context);
            if (candidate == null) {
                continue;
            }
            if (best == null) {
                best = candidate;
                continue;
            }
            if (isBetter(candidate, best)) {
                best = candidate;
            }
        }

        if (best != null && best.getMatchLevel() != ShippingQuote.MatchLevel.NONE) {
            return best;
        }

        ShippingQuote fallback = chooseFallback(shippers);
        if (fallback != null && fallback.getMatchLevel().getPriority() < ShippingQuote.MatchLevel.GLOBAL.getPriority()) {
            fallback.setMatchLevel(ShippingQuote.MatchLevel.GLOBAL);
        }
        return fallback != null ? fallback : best;
    }

    private static ShippingQuote buildQuote(Shipper shipper, AddressContext context) {
        if (shipper == null || shipper.getName() == null) {
            return null;
        }
        ShippingQuote quote = new ShippingQuote();
        quote.setShipperId(shipper.getId());
        quote.setShipperName(shipper.getName());
        quote.setEstimatedTime(shipper.getEstimatedTime());
        quote.setServiceArea(shipper.getServiceArea());
        quote.setFee(normalizeFee(shipper.getBaseFee()));
        quote.setMatchLevel(determineMatchLevel(shipper.getServiceArea(), context));
        return quote;
    }

    private static ShippingQuote chooseFallback(List<Shipper> shippers) {
        ShippingQuote fallback = null;
        for (Shipper shipper : shippers) {
            if (shipper == null || shipper.getName() == null) {
                continue;
            }
            ShippingQuote candidate = new ShippingQuote();
            candidate.setShipperId(shipper.getId());
            candidate.setShipperName(shipper.getName());
            candidate.setEstimatedTime(shipper.getEstimatedTime());
            candidate.setServiceArea(shipper.getServiceArea());
            candidate.setFee(normalizeFee(shipper.getBaseFee()));
            candidate.setMatchLevel(ShippingQuote.MatchLevel.GLOBAL);
            if (fallback == null || isBetter(candidate, fallback)) {
                fallback = candidate;
            }
        }
        return fallback;
    }

    private static boolean isBetter(ShippingQuote candidate, ShippingQuote current) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }
        int priorityDiff = candidate.getMatchLevel().getPriority() - current.getMatchLevel().getPriority();
        if (priorityDiff != 0) {
            return priorityDiff > 0;
        }
        BigDecimal candidateFee = normalizeFee(candidate.getFee());
        BigDecimal currentFee = normalizeFee(current.getFee());
        int feeComparison = candidateFee.compareTo(currentFee);
        if (feeComparison != 0) {
            return feeComparison < 0;
        }
        Long candidateId = candidate.getShipperId();
        Long currentId = current.getShipperId();
        if (candidateId == null) {
            return false;
        }
        if (currentId == null) {
            return true;
        }
        return candidateId < currentId;
    }

    private static ShippingQuote.MatchLevel determineMatchLevel(String serviceArea, AddressContext context) {
        if (serviceArea == null || serviceArea.trim().isEmpty()) {
            return ShippingQuote.MatchLevel.GLOBAL;
        }
        NormalizedText area = NormalizedText.from(serviceArea);
        if (area.isEmpty()) {
            return ShippingQuote.MatchLevel.GLOBAL;
        }

        if (containsAnyKeyword(area, GLOBAL_KEYWORDS)) {
            return ShippingQuote.MatchLevel.GLOBAL;
        }

        ShippingQuote.MatchLevel best = ShippingQuote.MatchLevel.NONE;
        for (String part : splitArea(serviceArea)) {
            NormalizedText partText = NormalizedText.from(part);
            if (partText.isEmpty()) {
                continue;
            }
            ShippingQuote.MatchLevel level = matchPart(partText, context);
            if (level.getPriority() > best.getPriority()) {
                best = level;
                if (best == ShippingQuote.MatchLevel.DISTRICT) {
                    break;
                }
            }
        }
        return best;
    }

    private static ShippingQuote.MatchLevel matchPart(NormalizedText part, AddressContext context) {
        if (containsAnyKeyword(part, GLOBAL_KEYWORDS)) {
            return ShippingQuote.MatchLevel.GLOBAL;
        }
        String matchedRegion = REGION_ALIAS_MAP.get(part.withSpaces);
        if (matchedRegion == null) {
            matchedRegion = REGION_ALIAS_MAP.get(part.withoutSpaces);
        }
        if (matchedRegion != null && matchedRegion.equals(context.regionCode)) {
            return ShippingQuote.MatchLevel.REGION;
        }

        if (containsToken(part, context.provinceTokens)) {
            return ShippingQuote.MatchLevel.PROVINCE;
        }
        if (containsToken(part, context.cityTokens)) {
            return ShippingQuote.MatchLevel.CITY;
        }
        if (containsToken(part, context.districtTokens)) {
            return ShippingQuote.MatchLevel.DISTRICT;
        }

        return ShippingQuote.MatchLevel.NONE;
    }

    private static List<String> splitArea(String serviceArea) {
        if (serviceArea == null) {
            return Collections.emptyList();
        }
        String[] parts = serviceArea.split("[,;\\n\\|/]+");
        List<String> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part != null && !part.trim().isEmpty()) {
                result.add(part.trim());
            }
        }
        if (result.isEmpty()) {
            result.add(serviceArea.trim());
        }
        return result;
    }

    private static boolean containsAnyKeyword(NormalizedText text, Set<String> keywords) {
        if (keywords.isEmpty() || text.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword == null || keyword.isEmpty()) {
                continue;
            }
            if (text.withSpaces.contains(keyword) || text.withoutSpaces.contains(keyword.replace(" ", ""))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsToken(NormalizedText part, Set<String> tokens) {
        if (tokens.isEmpty() || part.isEmpty()) {
            return false;
        }
        for (String token : tokens) {
            if (token == null || token.isEmpty()) {
                continue;
            }
            if (part.withSpaces.contains(token)) {
                return true;
            }
            String compactToken = token.replace(" ", "");
            if (!compactToken.isEmpty() && part.withoutSpaces.contains(compactToken)) {
                return true;
            }
        }
        return false;
    }

    private static BigDecimal normalizeFee(BigDecimal fee) {
        if (fee == null) {
            return BigDecimal.ZERO;
        }
        if (fee.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return fee;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String replaced = value.replace('Đ', 'D').replace('đ', 'd');
        String normalized = Normalizer.normalize(replaced, Normalizer.Form.NFD);
        String stripped = normalized.replaceAll("\\p{M}+", "");
        String lower = stripped.toLowerCase(Locale.ROOT);
        String cleaned = lower.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private static String normalizeKey(String value) {
        return normalize(value).replace(" ", "");
    }

    private static Set<String> buildTokens(String value) {
        if (value == null) {
            return Collections.emptySet();
        }
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> tokens = new LinkedHashSet<>();
        tokens.add(normalized);
        String stripped = stripPrefixes(normalized);
        if (!stripped.equals(normalized)) {
            tokens.add(stripped);
        }
        String compact = normalized.replace(" ", "");
        if (!compact.isEmpty()) {
            tokens.add(compact);
        }
        String strippedCompact = stripped.replace(" ", "");
        if (!strippedCompact.isEmpty()) {
            tokens.add(strippedCompact);
        }
        addSynonyms(tokens, normalized);
        addSynonyms(tokens, stripped);
        return tokens;
    }

    private static void addSynonyms(Set<String> tokens, String normalizedValue) {
        if (normalizedValue == null || normalizedValue.isEmpty()) {
            return;
        }
        String value = normalizedValue.trim();
        String compact = value.replace(" ", "");
        boolean isHcm = value.contains("ho chi minh") || value.contains("tp ho chi minh")
                || value.contains("tp hcm") || value.contains("tphcm") || value.contains("hcm")
                || value.contains("sai gon") || value.contains("saigon") || compact.contains("hochiminh");
        if (isHcm) {
            tokens.add("ho chi minh");
            tokens.add("hochiminh");
            tokens.add("tp ho chi minh");
            tokens.add("tphochiminh");
            tokens.add("tp hcm");
            tokens.add("tphcm");
            tokens.add("hcm");
            tokens.add("sai gon");
            tokens.add("saigon");
        }
        boolean isHaNoi = value.contains("ha noi") || compact.contains("hanoi") || value.contains("hn");
        if (isHaNoi) {
            tokens.add("ha noi");
            tokens.add("hanoi");
            tokens.add("hn");
        }
        boolean isDaNang = value.contains("da nang") || compact.contains("danang") || value.contains("dn");
        if (isDaNang) {
            tokens.add("da nang");
            tokens.add("danang");
            tokens.add("dn");
        }
    }


    private static String stripPrefixes(String value) {
        String result = value;
        for (String prefix : ADMIN_PREFIXES) {
            if (result.startsWith(prefix + " ")) {
                result = result.substring(prefix.length() + 1).trim();
            } else if (result.equals(prefix)) {
                return "";
            } else if (result.startsWith(prefix)) {
                result = result.substring(prefix.length()).trim();
            }
        }
        return result;
    }

    private static Map<String, String> createProvinceRegionMap() {
        Map<String, String> map = new HashMap<>();
        addProvinces(map, "mien-bac",
                "Hà Nội", "Hải Phòng", "Quảng Ninh", "Bắc Ninh", "Bắc Giang", "Hưng Yên", "Hải Dương", "Hà Nam",
                "Vĩnh Phúc", "Thái Nguyên", "Phú Thọ", "Bắc Kạn", "Cao Bằng", "Lạng Sơn", "Tuyên Quang",
                "Hà Giang", "Lào Cai", "Yên Bái", "Điện Biên", "Lai Châu", "Sơn La", "Hòa Bình", "Nam Định",
                "Ninh Bình", "Thái Bình");
        addProvinces(map, "mien-trung",
                "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị", "Thừa Thiên Huế", "Đà Nẵng",
                "Quảng Nam", "Quảng Ngãi", "Bình Định", "Phú Yên", "Khánh Hòa", "Ninh Thuận", "Bình Thuận",
                "Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng");
        addProvinces(map, "mien-nam",
                "Hồ Chí Minh", "Bà Rịa - Vũng Tàu", "Bình Dương", "Bình Phước", "Đồng Nai", "Tây Ninh",
                "Long An", "Tiền Giang", "Bến Tre", "Vĩnh Long", "Trà Vinh", "Đồng Tháp", "An Giang",
                "Cần Thơ", "Hậu Giang", "Kiên Giang", "Sóc Trăng", "Bạc Liêu", "Cà Mau");
        return map;
    }

    private static void addProvinces(Map<String, String> map, String regionCode, String... provinces) {
        for (String province : provinces) {
            map.put(normalizeKey(province), regionCode);
        }
    }

    private static Map<String, String> createRegionAliasMap() {
        Map<String, String> map = new HashMap<>();
        map.put("mien bac", "mien-bac");
        map.put("bac bo", "mien-bac");
        map.put("phia bac", "mien-bac");
        map.put("dong bang song hong", "mien-bac");
        map.put("mien trung", "mien-trung");
        map.put("trung bo", "mien-trung");
        map.put("tay nguyen", "mien-trung");
        map.put("mien nam", "mien-nam");
        map.put("nam bo", "mien-nam");
        map.put("dong nam bo", "mien-nam");
        map.put("tay nam bo", "mien-nam");
        map.put("dong bang song cuu long", "mien-nam");
        return map;
    }

    private static Set<String> createGlobalKeywords() {
        return new HashSet<>(Arrays.asList(
                "toan quoc", "ca nuoc", "nationwide", "tat ca", "global", "all provinces", "toanquoc"
        ));
    }

    private static final class AddressContext {
        private final Set<String> districtTokens;
        private final Set<String> cityTokens;
        private final Set<String> provinceTokens;
        private final String regionCode;
        private final boolean supportedCountry;

        private AddressContext(UserAddress address) {
            this.districtTokens = new LinkedHashSet<>(buildTokens(address.getDistrict()));
            this.cityTokens = new LinkedHashSet<>(buildTokens(address.getCity()));
            this.provinceTokens = new LinkedHashSet<>(buildTokens(address.getProvince()));
            this.regionCode = resolveRegion(address);
            this.supportedCountry = evaluateSupportedCountry(address);
        }

        private boolean evaluateSupportedCountry(UserAddress address) {
            if (address == null) {
                return true;
            }
            String country = address.getCountry();
            if (country == null || country.isBlank()) {
                // Legacy dữ liệu không lưu country -> mặc định giao trong nước
                return true;
            }
            String normalized = normalize(country);
            if (normalized.isEmpty()) {
                return true;
            }
            if (SUPPORTED_COUNTRIES.contains(normalized)) {
                return true;
            }
            String compact = normalized.replace(" ", "");
            return SUPPORTED_COUNTRIES.contains(compact);
        }

        private boolean isSupportedCountry() {
            return supportedCountry;
        }

        private String resolveRegion(UserAddress address) {
            List<String> probe = new ArrayList<>();
            probe.add(address.getProvince());
            probe.add(address.getCity());
            probe.add(address.getDistrict());

            for (String value : probe) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                String key = normalizeKey(value);
                if (key.isEmpty()) {
                    continue;
                }
                String region = PROVINCE_REGION_MAP.get(key);
                if (region != null) {
                    return region;
                }
            }
            return null;
        }
    }

    private static final class NormalizedText {
        private final String withSpaces;
        private final String withoutSpaces;

        private NormalizedText(String withSpaces, String withoutSpaces) {
            this.withSpaces = withSpaces;
            this.withoutSpaces = withoutSpaces;
        }

        static NormalizedText from(String value) {
            String normalized = normalize(value);
            if (normalized.isEmpty()) {
                return new NormalizedText("", "");
            }
            return new NormalizedText(normalized, normalized.replace(" ", ""));
        }

        boolean isEmpty() {
            return withSpaces.isEmpty() && withoutSpaces.isEmpty();
        }
    }
}




