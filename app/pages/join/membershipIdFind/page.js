import MembershipHeader from "@/app/components/MembershipHeader";

export default function MembershipIdFind() {
  return (
    <div className="h-project-template">
      <MembershipHeader title="아이디 찾기" nowPage="MembershipIdFind" />
      <div className="h-project-content join-membership">
        <h3 className="join-tit">보호자 이름, 휴대폰 번호를 적어주세요</h3>
        <div className="tab-template">
          <div className="input-area">
            <input
              type="num"
              className="default-input"
              id="find-id-birth"
              placeholder="보호자 이름"
            />
            <label htmlFor="find-id-birth" className="blind">
              보호자 이름
            </label>
          </div>
          <div className="input-area">
            <input
              type="num"
              className="default-input"
              id="find-id-phone"
              placeholder="휴대폰 번호"
            />
            <label htmlFor="find-id-phone" className="blind">
              휴대폰 번호
            </label>
          </div>
          <div className="bottom-fixed">
            <button type="button" className="default-block-btn">
              아이디 찾기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
