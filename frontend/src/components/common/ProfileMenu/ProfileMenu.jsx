import styles from "./ProfileMenu.module.css";

function ProfileMenu() {
  const email = localStorage.getItem("email") || "User";
  const name = email.split("@")[0];

  const displayName =
    name.charAt(0).toUpperCase() + name.slice(1);

  const initial = name.charAt(0).toUpperCase();

  return (
    <div className={styles.profile}>

      <div className={styles.avatar}>
        {initial}
      </div>

      <div className={styles.info}>
        <h4>{displayName}</h4>
        <span>Personal Account</span>
      </div>

    </div>
  );
}

export default ProfileMenu;