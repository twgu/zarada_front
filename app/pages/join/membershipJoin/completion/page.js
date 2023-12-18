"use client";

import { useRouter } from "next/navigation";

import MembershipHeader from "@/app/components/MembershipHeader";

export default function Completion() {
  const router = useRouter();

  return (
    <div className="h-project-template">
      <MembershipHeader title="회원가입" />
      <div className="h-project-content join-membership">
        <h3 className="completion-tit">자라다(ZARADA) App</h3>
        <span className="completion-txt">회원가입 되었습니다!</span>
        <div className="bottom-fixed">
          <button
            className="default-block-btn"
            onClick={() => {
              router.push("/pages/login", { scroll: false });
            }}
          >
            가입완료
          </button>
        </div>
      </div>
    </div>
  );
}
