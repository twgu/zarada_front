"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

import MembershipHeader from "@/app/components/MembershipHeader";

export default function Success() {
  const [findId, setFindId] = useState("");

  useEffect(() => {
    setFindId(localStorage.getItem("findId") ? localStorage.getItem("findId") : "");
  }, []);

  return (
    <div className="h-project-template">
      <MembershipHeader title="아이디 찾기" />
      <div className="h-project-content join-membership id-find">
        <h3 className="join-tit">일치하는 아이디를 찾았습니다</h3>
        <div className="input-area">
          <input
            type="text"
            className="default-input"
            value={findId}
            disabled
          />
        </div>
        <div className="bottom-fixed">
          <Link
            role="button"
            href="/pages/login"
            className="default-block-btn"
            scroll={false}
          >
            로그인
          </Link>
          <Link
            role="button"
            href="/pages/join/membershipPwFind"
            className="default-block-btn pw-find-btn"
            scroll={false}
          >
            비밀번호 찾기
          </Link>
        </div>
      </div>
    </div>
  );
}
