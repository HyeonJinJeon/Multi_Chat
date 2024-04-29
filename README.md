# Multi_Chat 💬



## 개발환경
- 개발언어 : Java
- 개발환경 : [Github](https://github.com/), [IntelliJ](https://www.jetbrains.com/ko-kr/idea/)

## 📽️ Project Info.

- 프로젝트 설명 : Java의 Socket과 Thread를 사용하여 여러 Client 간의 채팅 시스템
- 착수 일 : 2024년 4월 26일 금
- 마감 일 : 2024년 4월 29일 월

## 프로그램 기능 및 사용법
1. 사용자 닉네임 입력(닉네임 중복시 재입력)
   
2. 닉네임 입력 시 로비 입장
   
3. 로비에서 수행 가능한 명령어
  - 전체 사용자 목록 보기 : /users
  - 방 목록 보기 : /list
  - 방 생성 : /create
  - 방 입장 : /join
  - 접속종료 : /bye

4. 채팅방에서 수행 가능한 명령어
  - 전체 사용자 목록 보기 : /users
  - 접속한 채팅방 사용자 목록 보기 : /roomusers
  - 귓속말 기능 : /to
  - 사용자 차단 : /mute [사용자이름]
  - 사용자 차단 취소 : /cancelMute [사용자이름]
  - 방 나가기 : /exit

5. 채팅 입력 시 채팅방 별로 Text파일에 저장

6. 비속어 필터링 기능 (혐오 발언 길이만큼 *로 대체)

7. 차단한 사용자에게는 메세지 전송하지 않음

## 추가적으로 구현할 기능
1. 차단한 사용자로부터 메시지를 받지 않는 기능

2. 메시지 알림음 기능