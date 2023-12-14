"use client";

export default function FormGroup(props) {
  return (
    <div className={props.error ? "form-group error" : "form-group valid"}>
      {props.children}
      {props.error ? (
        <div className="form-error is-visible">
          <div data-validation-attr={props.errorText}>{props.errorText}</div>
        </div>
      ) : null}
    </div>
  );
}
