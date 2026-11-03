import { FaBolt, FaMobileScreen } from "react-icons/fa6";
import styles from "./UpcomingBills.module.css";

function UpcomingBills() {
  const bills = [
    {
      id: 1,
      title: "Airtel Recharge",
      due: "Due in 2 days",
      amount: "₹579.00",
      icon: <FaMobileScreen />,
      iconClass: styles.airtelIcon,
    },
    {
      id: 2,
      title: "Electricity Bill",
      due: "Due in 5 days",
      amount: "₹1,245.50",
      icon: <FaBolt />,
      iconClass: styles.electricIcon,
    },
  ];

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <h3 className={styles.title}>Upcoming Bills</h3>
        <span className={styles.dueBadge}>2 Bills Due</span>
      </div>

      <div className={styles.billList}>
        {bills.map((bill) => (
          <div key={bill.id} className={styles.billItem}>
            <div className={styles.left}>
              <div className={`${styles.iconWrap} ${bill.iconClass}`}>
                {bill.icon}
              </div>
              <div className={styles.info}>
                <span className={styles.billName}>{bill.title}</span>
                <span className={styles.billDue}>{bill.due}</span>
              </div>
            </div>
            <strong className={styles.amount}>{bill.amount}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}

export default UpcomingBills;
