using System.Collections;
using ProjectRelay.Player;
using ProjectRelay.World;
using UnityEngine;
using UnityEngine.UI;

namespace ProjectRelay.UI
{
    public class TutorialHintPopup : MonoBehaviour
    {
        [SerializeField] private PlayerTerrainState terrainState;
        [SerializeField] private GameObject root;
        [SerializeField] private Text hintText;
        [SerializeField] private float showSeconds = 2.5f;

        private Coroutine _co;
        private bool _shownCaution;
        private bool _shownDanger;

        private void Awake()
        {
            if (terrainState == null)
                terrainState = FindObjectOfType<PlayerTerrainState>();

            if (root != null)
                root.SetActive(false);
        }

        private void OnEnable()
        {
            if (terrainState != null)
                terrainState.RiskLevelChanged += OnRiskChanged;
        }

        private void OnDisable()
        {
            if (terrainState != null)
                terrainState.RiskLevelChanged -= OnRiskChanged;
        }

        private void OnRiskChanged(TerrainRiskLevel risk)
        {
            if (risk == TerrainRiskLevel.Caution && !_shownCaution)
            {
                _shownCaution = true;
                Show("주의 구간: 속도를 줄이고 하중을 관리해.");
            }
            else if (risk == TerrainRiskLevel.Dangerous && !_shownDanger)
            {
                _shownDanger = true;
                Show("위험 구간: 슬립 확률 상승! 우회 경로를 고려해.");
            }
        }

        private void Show(string msg)
        {
            if (root == null || hintText == null) return;

            if (_co != null)
                StopCoroutine(_co);

            hintText.text = msg;
            root.SetActive(true);
            _co = StartCoroutine(CoHide());
        }

        private IEnumerator CoHide()
        {
            yield return new WaitForSeconds(showSeconds);
            if (root != null) root.SetActive(false);
            _co = null;
        }
    }
}
