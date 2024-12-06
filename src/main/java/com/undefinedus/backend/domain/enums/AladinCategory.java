package com.undefinedus.backend.domain.enums;

// TODO : 안쓰이는데 마지막 까지 확인해 보고 필요없으면 지울 예정
public enum AladinCategory {
    // 국내도서
    DOMESTIC_ALL("국내도서"),
    DOMESTIC_NOVEL("국내도서>소설"),
    DOMESTIC_POETRY("국내도서>시/에세이"),
    DOMESTIC_ECONOMY("국내도서>경제경영"),
    DOMESTIC_SELF_IMPROVEMENT("국내도서>자기계발"),
    DOMESTIC_HUMANITIES("국내도서>인문"),
    DOMESTIC_HISTORY("국내도서>역사"),
    DOMESTIC_RELIGION("국내도서>종교"),
    DOMESTIC_POLITICS("국내도서>정치/사회"),
    DOMESTIC_ART("국내도서>예술/대중문화"),
    DOMESTIC_SCIENCE("국내도서>과학"),
    DOMESTIC_TECHNOLOGY("국내도서>기술/공학"),
    DOMESTIC_COMPUTER("국내도서>컴퓨터/IT"),
    DOMESTIC_REFERENCE("국내도서>참고서"),
    DOMESTIC_MAGAZINES("국내도서>잡지"),
    DOMESTIC_COMICS("국내도서>만화"),
    DOMESTIC_CHILDREN("국내도서>아동"),
    DOMESTIC_YOUNG_ADULT("국내도서>청소년"),
    DOMESTIC_TRAVEL("국내도서>여행"),
    DOMESTIC_COOKING("국내도서>요리"),
    DOMESTIC_HEALTH("국내도서>건강"),
    DOMESTIC_FAMILY("국내도서>가정/육아"),
    DOMESTIC_HOBBY("국내도서>취미/실용/스포츠"),
    DOMESTIC_EDUCATION("국내도서>교육"),
    
    // 외국도서
    FOREIGN_ALL("외국도서"),
    FOREIGN_FICTION("외국도서>소설/시/희곡"),
    FOREIGN_HUMANITIES("외국도서>인문/사회"),
    FOREIGN_BUSINESS("외국도서>경제/경영"),
    FOREIGN_SELF_HELP("외국도서>자기계발"),
    FOREIGN_SCIENCE("외국도서>자연과학"),
    FOREIGN_TECHNOLOGY("외국도서>기술공학"),
    FOREIGN_ARTS("외국도서>예술/대중문화"),
    FOREIGN_CHILDREN("외국도서>아동/청소년"),
    FOREIGN_LANGUAGE("외국도서>언어"),
    FOREIGN_LIFESTYLE("외국도서>생활/취미"),
    FOREIGN_TRAVEL("외국도서>여행"),
    FOREIGN_MAGAZINES("외국도서>해외잡지"),
    FOREIGN_COMICS("외국도서>그래픽노블"),
    
    // eBook
    EBOOK_ALL("eBook"),
    EBOOK_NOVEL("eBook>소설"),
    EBOOK_POETRY("eBook>시/에세이"),
    EBOOK_ECONOMY("eBook>경제경영"),
    EBOOK_HUMANITIES("eBook>인문"),
    EBOOK_SOCIETY("eBook>정치/사회"),
    EBOOK_SCIENCE("eBook>과학"),
    EBOOK_COMPUTER("eBook>컴퓨터/IT"),
    EBOOK_CHILDREN("eBook>아동"),
    EBOOK_YOUNG_ADULT("eBook>청소년"),
    EBOOK_COMICS("eBook>만화"),
    EBOOK_FOREIGN("eBook>외국도서"),
    
    // 음반/DVD
    CD_DVD_ALL("음반/DVD"),
    CD_DVD_DOMESTIC("음반/DVD>국내음반"),
    CD_DVD_FOREIGN("음반/DVD>외국음반"),
    CD_DVD_CLASSIC("음반/DVD>클래식"),
    CD_DVD_DVD("음반/DVD>DVD"),
    CD_DVD_BLURAY("음반/DVD>블루레이"),
    
    // 기타
    USED("중고도서"),
    FOREIGN_USED("외국도서중고"),
    GIFT("음반/DVD중고");
    
    private final String category;
    
    AladinCategory(String category) {
        this.category = category;
    }
    
    public String getCategory() {
        return this.category;
    }
}
