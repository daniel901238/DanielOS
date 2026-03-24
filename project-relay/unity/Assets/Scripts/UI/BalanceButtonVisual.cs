using ProjectRelay.Player;
using UnityEngine;
using UnityEngine.UI;

namespace ProjectRelay.UI
{
    public class BalanceButtonVisual : MonoBehaviour
    {
        [SerializeField] private BalanceAssistController balanceAssist;
        [SerializeField] private Image targetImage;
        [SerializeField] private Color idleColor = new Color(1f, 1f, 1f, 0.85f);
        [SerializeField] private Color activeColor = new Color(0.3f, 0.9f, 1f, 1f);

        private void Awake()
        {
            if (balanceAssist == null)
                balanceAssist = FindObjectOfType<BalanceAssistController>();
        }

        private void Update()
        {
            if (targetImage == null || balanceAssist == null) return;
            targetImage.color = balanceAssist.IsActive ? activeColor : idleColor;
        }
    }
}
