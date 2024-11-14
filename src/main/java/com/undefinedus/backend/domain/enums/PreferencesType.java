package com.undefinedus.backend.domain.enums;

public enum PreferencesType {

    건강_취미_레저 (55890),
    경제경영 (170),
    과학 (987),
    만화 (2551),
    사회과학 (798),
    소설_시_희곡 (1),
    어린이 (1108),
    에세이 (55889),
    여행 (1196),
    역사 (74),
    예술_대중문화 (517),
    외국어 (1322),
    가정_요리_뷰티 (1230),
    유아 (13789),
    인문학 (656),
    자기계발 (336),
    잡지 (2913),
    장르소설 (112011),
    종교_역학 (1237),
    청소년 (1137),
    컴퓨터_모바일 (351);

    private final int categoryId;  // 카테고리 숫자 값

    PreferencesType(int categoryCode) {
        this.categoryId = categoryCode;
    }

    public int getCategoryId() {
        return categoryId;
    }
}