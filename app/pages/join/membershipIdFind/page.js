import MembershipHeader from "@/app/components/MembershipHeader";
import ValidationInput from "@/app/components/ValidationInput";

export default function MembershipIdFind() {
  return (
    <div className="h-project-template">
      <MembershipHeader title="아이디 찾기" nowPage="MembershipIdFind" />
      <div className="h-project-content join-membership">
        <h3 className="join-tit">보호자 이름, 휴대폰 번호를 적어주세요</h3>
        <div className="tab-template">
          <ValidationInput
            id="find-id-birth"
            type="text"
            placeholder="보호자 이름"
          />
          <ValidationInput
            id="find-id-phone"
            type="text"
            placeholder="휴대폰 번호"
          />
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
