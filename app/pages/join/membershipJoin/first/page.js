"use client";

import { useState } from "react";
import MembershipHeader from "@/app/components/MembershipHeader";
import { useRouter } from "next/navigation";

export default function First() {
  const router = useRouter();

  const [agree, setAgree] = useState(false);
  const [popOpen, setPopOpen] = useState(false);

  const fNextStep = () => {
    if (!agree) {
      alert("개인정보 수집 및 이용 동의에 체크해주세요.");
    } else {
      router.push("/pages/join/membershipJoin/second", {
        scroll: false,
      });
    }
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="회원가입" />
      <div className="h-project-content join-membership agree-membership">
        <div className="loading-bar first"></div>
        <button
          tpye="button"
          className={agree ? "border-block-btn active" : "border-block-btn"}
          onClick={() => {
            setAgree(!agree);
          }}
        >
          전체동의
        </button>
        <div className="agree-chk-area">
          <input
            type="checkbox"
            id="agree01"
            name="agree-first"
            className="default-chk"
            onChange={() => {
              setAgree(!agree);
            }}
            checked={agree}
          />
          <label htmlFor="agree01" className="default-label">
            <span className="default-label-txt"></span>개인정보 수집 및 이용
            동의
          </label>
          <div className="pop-container">
            <button
              type="button"
              className="pop-open-btn"
              onClick={() => {
                setPopOpen(true);
              }}
            >
              <span className="agree-pop-btn">보기</span>
            </button>
            {popOpen ? (
              <div className="popup">
                <div className="black-section"></div>
                <div className="content-section-wrapper">
                  <div className="agree-desc privacy-wrap">
                    <p className="privacy-tit">
                      개인정보활용동의 및 교육확인서
                    </p>
                    <p className="privacy-txt">
                      본인은「개인정보보호법」제 15조 및 제22조에 의하여
                      동아ST(이하 “당사”라고 한다)가 아래의 내용과 같이 본인의
                      개인정보를 수집 및 이용하는데 동의합니다.
                    </p>
                    <ul className="privacy-list">
                      <li>
                        <p className="privacy-s-tit">
                          가. 개인정보의 수집.이용목적
                        </p>
                        <p className="privacy-txt">
                          당사는
                          <span className="privacy-point">
                            성장호르몬을 자가 투여하는 환자의 주사 교육을 위한
                            목적
                          </span>
                          으로 개인정보를 수집 및 이용하고자 합니다.
                        </p>
                      </li>
                      <li>
                        <p className="privacy-s-tit">
                          나. 동아ST㈜가 수집, 이용할 개인정보의 내용
                        </p>
                        <p className="privacy-txt">1) 필수적 동의사항 :</p>
                        <ul>
                          <li>
                            ① 개인 식별정보 : 투여자명, 주소, 전화번호
                            <br /> - 원활한 교육 진행 및 부속품 제공
                          </li>
                          <li>
                            ② 투여교육을 받을 제품종류
                            <br /> - 환자에게 정확한 제품 교육을 하기 위함
                          </li>
                        </ul>
                        <p className="privacy-txt">2) 선택적 동의사항</p>
                        <ul>
                          <li>
                            ① 처방병원, 처방의사
                            <br /> - 약물 투여중, 부작용등 기타 상황에 대한 정보
                            수집의 목적
                          </li>
                        </ul>
                      </li>
                      <li>
                        <p className="privacy-s-tit">
                          다. 개인정보의 보유 및 이용기간
                        </p>
                        <p className="privacy-txt">
                          위 개인정보는
                          <span className="privacy-point">
                            수집, 이용에 관한 동의일로부터 투여자가 투여를
                            중단할 때까지(보통의 경우 2년) 또는 정보주체가
                            개인정보 삭제를 요청할 경우
                          </span>
                          지체 없이 파기합니다.
                        </p>
                      </li>
                      <li>
                        <p className="privacy-s-tit">
                          라. 동의를 거부할 권리 및 동의를 거부할 경우의 불이익
                        </p>
                        <p className="privacy-txt">
                          귀하는 상기 내용의 동의를 거부할 수 있으며, 동의하지
                          않더라도 성장호르몬 자가 투여 설명에 관한 제약은
                          없습니다.
                          <span className="privacy-warning">
                            단, 동의를 거부하실 경우 이용자 확인이 불가하여
                            부속품 제공, 재교육등 서비스 상의 불이익을 받을 수
                            있음을 알려 드립니다.
                          </span>
                        </p>
                        <p className="privacy-etc">
                          ※ 당사에서는 귀하의 소중한 개인정보 보호를 위해
                          주민등록번호를 수집하지 않으며 제공받은 개인정보를
                          위가.항의수집, 이용 목적 외의 어떠한 목적으로도 절대
                          사용하지 않습니다.
                        </p>
                      </li>
                    </ul>
                    <p className="privacy-line"></p>
                    <p className="privacy-txt">
                      처방받아 온 그로트로핀-Ⅱ 제품은 냉장보관 하도록
                      합니다.냉장보관의 조건은 2~8℃ 입니다.
                      <br /> <br /> 상기 자필한 대로 교육간호사로부터 제품의
                      보관방법에 대해 주의 의무를 교육받았음을 확인합니다.
                    </p>
                  </div>
                  <button
                    type="button"
                    className="close-button adaptLink"
                    onClick={() => {
                      setPopOpen(false);
                    }}
                  >
                    <span>닫기</span>
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
        <div className="bottom-fixed">
          <button
            type="button"
            className="default-block-btn agree-next-btn"
            onClick={fNextStep}
          >
            다 음
          </button>
        </div>
      </div>
    </div>
  );
}
