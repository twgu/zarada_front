import Link from "next/link";
import MembershipHeader from "@/app/components/MembershipHeader";

export default function Fail() {
  return (
    <div className="h-project-template">
      <MembershipHeader title="아이디 찾기" />
      <div className="h-project-content join-membership id-find">
        <h3 className="join-tit">일치하는 아이디가 없습니다.</h3>
        <div className="input-area">
          <span className="fail-message">
            다시 회원가입을 해주시거나, 다시 한번 확인해주세요.
          </span>
        </div>
        <div className="bottom-fixed">
          <Link
            role="button"
            href="/pages/login"
            className="default-block-btn"
            scroll={false}
          >
            확인
          </Link>
        </div>
      </div>
    </div>
  );
}
