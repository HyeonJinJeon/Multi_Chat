class BadWords {
    String[] koreaBadWords = {
            //심한 비속어가 많은 관계로 테스트용으로 몇개만 작성
            "나쁜놈" ,"쓰레기", "바보", "멍청이", "똥개"
    };

    public String[] getKoreaBadWords() {
        return koreaBadWords;
    }

    //욕설 필터링 구현
    public String filteringBadWords(String msg){
        String filteringMsg = msg;
        for(int i = 0; i < koreaBadWords.length; i++){
            if(msg.contains(koreaBadWords[i])){
                String star = "";
                //욕설의 길이만큼 *로 대체
                for(int j = 0; j < koreaBadWords[i].length(); j++){
                    star += "*";
                }
                // 필터링된 메시지를 기존 문자열 대신 star로 대체하여 filteringMsg에 저장
                filteringMsg = filteringMsg.replaceAll(koreaBadWords[i], star);
            }
        }
        return filteringMsg;
    }
}